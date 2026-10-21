package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.addOrganization
import de.klg71.keycloakmigration.keycloakapi.allOrganizationMembers
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddOrganization
import de.klg71.keycloakmigration.keycloakapi.model.Organization
import de.klg71.keycloakmigration.keycloakapi.organizationByAlias
import de.klg71.keycloakmigration.keycloakapi.realmExistsById

private const val MANAGED_MEMBERSHIP = "MANAGED"

class DeleteOrganizationAction(
    realm: String?,
    private val alias: String,
    private val deleteManagedMembers: Boolean = false
) : Action(realm) {

    private lateinit var original: Organization
    private var linkedIdentityProviders: List<String> = emptyList()
    private var unmanagedMembers: List<String> = emptyList()

    override fun execute() {
        if (!client.realmExistsById(realm()))
            throw MigrationException("Realm with id: ${realm()} does not exist!")

        original = client.organizationByAlias(alias, realm())
        linkedIdentityProviders = client.organizationIdentityProviders(realm(), original.id.toString())
            .map { it.alias }

        // Keycloak deletes managed members' user accounts together with the organization
        val (managed, unmanaged) = client.allOrganizationMembers(realm(), original.id)
            .partition { it.membershipType == MANAGED_MEMBERSHIP }
        if (managed.isNotEmpty() && !deleteManagedMembers)
            throw MigrationException(
                "Organization with alias: $alias has ${managed.size} managed member(s), whose users Keycloak " +
                    "deletes with the organization. Set deleteManagedMembers: true to delete it anyway."
            )
        unmanagedMembers = unmanaged.map { it.id.toString() }

        client.deleteOrganization(original.id, realm()).run {
            if (!isSuccessful())
                throw MigrationException(
                    "Failed to delete organization with alias: $alias in realm: ${realm()}. " +
                        "Status: ${status()}, Reason: ${reason()}"
                )
        }
    }

    /**
     * Recreates the organization, re-links its identity providers and re-adds its unmanaged members.
     * Managed members' users were deleted by Keycloak and can't be restored.
     */
    override fun undo() {
        client.addOrganization(
            realm(),
            AddOrganization(original.name, original.alias, original.redirectUrl, original.domains, original.attributes)
        )
        val recreated = client.organizationByAlias(alias, realm())
        linkedIdentityProviders.forEach {
            client.linkIdentityProviderToOrganization(
                realm = realm(),
                orgId = recreated.id.toString(),
                identityProviderAlias = it
            )
        }
        unmanagedMembers.forEach { userId ->
            client.addOrganizationMember(realm(), recreated.id.toString(), userId).run {
                if (!isSuccessful())
                    throw MigrationException(
                        "Failed to re-add member $userId to organization with alias: $alias. " +
                            "Status: ${status()}, Reason: ${reason()}"
                    )
            }
        }
    }

    override fun name() = "DeleteOrganizationAction $alias"
}
