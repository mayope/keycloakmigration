package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.addOrganization
import de.klg71.keycloakmigration.keycloakapi.allOrganizationGroupMembers
import de.klg71.keycloakmigration.keycloakapi.allOrganizationGroups
import de.klg71.keycloakmigration.keycloakapi.allOrganizationMembers
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddOrganization
import de.klg71.keycloakmigration.keycloakapi.model.Organization
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationGroup
import de.klg71.keycloakmigration.keycloakapi.organizationByAlias
import de.klg71.keycloakmigration.keycloakapi.realmExistsById
import java.util.UUID

private const val UNMANAGED_MEMBERSHIP = "UNMANAGED"

class DeleteOrganizationAction(
    realm: String?,
    private val alias: String,
    private val deleteManagedMembers: Boolean = false
) : Action(realm) {

    private lateinit var original: Organization
    private var linkedIdentityProviders: List<String> = emptyList()
    private var unmanagedMembers: List<String> = emptyList()
    private var organizationGroups: List<OrganizationGroup> = emptyList()
    // group path -> ids of the users that were members of that group, captured before delete
    private var groupMemberships: Map<String, List<String>> = emptyMap()

    override fun execute() {
        if (!client.realmExistsById(realm()))
            throw MigrationException("Realm with id: ${realm()} does not exist!")

        original = client.organizationByAlias(alias, realm())
        linkedIdentityProviders = client.organizationIdentityProviders(realm(), original.id.toString())
            .map { it.alias }

        // Keycloak deletes managed members' user accounts together with the organization. Only members
        // Keycloak positively reports as UNMANAGED are treated as safe; a MANAGED, missing or unrecognised
        // membershipType counts as managed, so the guard fails safe rather than risking user deletion.
        val (unmanaged, managed) = client.allOrganizationMembers(realm(), original.id)
            .partition { it.membershipType == UNMANAGED_MEMBERSHIP }
        if (managed.isNotEmpty() && !deleteManagedMembers)
            throw MigrationException(
                "Organization with alias: $alias has ${managed.size} member(s) whose users Keycloak may " +
                    "delete with the organization (membershipType is not UNMANAGED). Set deleteManagedMembers: " +
                    "true to delete it anyway."
            )
        unmanagedMembers = unmanaged.map { it.id.toString() }

        // Captured before the delete so undo can rebuild the organization's group hierarchy and the
        // memberships within it. Memberships are keyed by group path, which is stable across the
        // recreate (ids are not), and only surviving (unmanaged) users can actually be re-added.
        organizationGroups = client.allOrganizationGroups(realm(), original.id)
        groupMemberships = captureGroupMemberships(original.id, organizationGroups)

        client.deleteOrganization(original.id, realm()).run {
            if (!isSuccessful())
                throw MigrationException(
                    "Failed to delete organization with alias: $alias in realm: ${realm()}. " +
                        "Status: ${status()}, Reason: ${reason()}"
                )
        }
    }

    /**
     * Recreates the organization, re-links its identity providers, restores its group hierarchy,
     * re-adds its unmanaged members and restores those members' group memberships.
     *
     * The recreated organization and groups get fresh ids, so groups are matched back to their
     * captured memberships by path. Managed members' users were deleted by Keycloak with the
     * organization and cannot be restored, so only surviving (unmanaged) users are re-added to the
     * organization and to their groups; a membership of a user that no longer exists is skipped.
     */
    override fun undo() {
        client.addOrganization(
            realm(),
            AddOrganization(original.name, original.alias, original.redirectUrl, original.domains, original.attributes)
        )
        val recreated = client.organizationByAlias(alias, realm())
        linkedIdentityProviders.forEach { identityProviderAlias ->
            client.linkIdentityProviderToOrganization(
                realm = realm(),
                orgId = recreated.id.toString(),
                identityProviderAlias = identityProviderAlias
            ).run {
                if (!isSuccessful())
                    throw MigrationException(
                        "Failed to re-link identity provider $identityProviderAlias to organization with " +
                            "alias: $alias. Status: ${status()}, Reason: ${reason()}"
                    )
            }
        }
        organizationGroups.forEach { group ->
            // Strip the captured id/path: with an id present Keycloak treats the POST as a move of an
            // existing group (which was deleted), returning 404. Without it, a new group is created.
            client.addOrganizationGroup(realm(), recreated.id.toString(), group.forCreate()).run {
                if (!isSuccessful())
                    throw MigrationException(
                        "Failed to restore group ${group.name} to organization with alias: $alias. " +
                            "Status: ${status()}, Reason: ${reason()}"
                    )
            }
        }
        // Members must be re-added to the organization before they can be re-added to its groups.
        unmanagedMembers.forEach { userId ->
            client.addOrganizationMember(realm(), recreated.id.toString(), userId).run {
                if (!isSuccessful())
                    throw MigrationException(
                        "Failed to re-add member $userId to organization with alias: $alias. " +
                            "Status: ${status()}, Reason: ${reason()}"
                    )
            }
        }
        restoreGroupMemberships(recreated.id)
    }

    /**
     * Walks the captured group tree and records, per group path, the ids of the users that were
     * members of each group. Group paths are stable across the recreate; group ids are not.
     */
    private fun captureGroupMemberships(
        orgId: UUID,
        groups: List<OrganizationGroup>
    ): Map<String, List<String>> {
        val memberships = mutableMapOf<String, List<String>>()
        fun visit(group: OrganizationGroup) {
            val path = group.path ?: return
            val groupId = group.id ?: return
            memberships[path] = client.allOrganizationGroupMembers(realm(), orgId, groupId)
                .map { it.id.toString() }
            group.subGroups?.forEach(::visit)
        }
        groups.forEach(::visit)
        return memberships
    }

    /**
     * Re-adds each captured membership to the recreated organization's groups, matching groups by
     * path. Users that no longer exist (managed members deleted with the organization) are skipped,
     * since they cannot and should not be re-added.
     */
    private fun restoreGroupMemberships(orgId: UUID) {
        if (groupMemberships.isEmpty()) return
        val survivingUsers = unmanagedMembers.toSet()
        val groupIdByPath = groupIdsByPath(client.allOrganizationGroups(realm(), orgId))

        groupMemberships.forEach { (path, userIds) ->
            val groupId = groupIdByPath[path] ?: return@forEach
            userIds.filter { it in survivingUsers }
                .forEach { userId -> restoreGroupMembership(orgId, groupId, path, userId) }
        }
    }

    /** Flattens the group tree into a path -> id map, so memberships can be matched by path. */
    private fun groupIdsByPath(groups: List<OrganizationGroup>): Map<String, UUID> =
        groups.flatMap { group ->
            val path = group.path
            val id = group.id
            val self = if (path != null && id != null) listOf(path to id) else emptyList()
            self + groupIdsByPath(group.subGroups ?: emptyList()).toList()
        }.toMap()

    private fun restoreGroupMembership(orgId: UUID, groupId: UUID, path: String, userId: String) {
        client.addOrganizationGroupMember(realm(), orgId.toString(), groupId.toString(), userId).run {
            if (!isSuccessful())
                throw MigrationException(
                    "Failed to restore membership of user $userId in group $path of organization " +
                        "with alias: $alias. Status: ${status()}, Reason: ${reason()}"
                )
        }
    }

    override fun name() = "DeleteOrganizationAction $alias"
}
