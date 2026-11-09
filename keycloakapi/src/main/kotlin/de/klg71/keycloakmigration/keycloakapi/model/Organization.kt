package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class OrganizationDomain(
    val name: String,
    val verified: Boolean = false
)

data class Organization(
    val id: UUID,
    val name: String,
    val alias: String?,
    val redirectUrl: String?,
    val domains: Set<OrganizationDomain>,
    val attributes: Map<String, List<String>>? = mapOf(),
)

data class OrganizationMember(
    val id: UUID,
    val username: String,
    val membershipType: String? = null
)

/**
 * An organization group as returned by GET .../organizations/{orgId}/groups and accepted as the body
 * of the matching POST. Only the fields needed to restore a deleted organization's group structure
 * are modelled: the name, its attributes, and its nested subgroups. Keycloak assigns a fresh id when
 * the group is recreated, and the id is read-only on create, so it is intentionally not sent back.
 */
data class OrganizationGroup(
    val id: UUID? = null,
    val name: String,
    val path: String? = null,
    val attributes: Map<String, List<String>>? = null,
    val subGroups: List<OrganizationGroup>? = null
) {
    /**
     * A copy suitable as a create body: the server-assigned id and path are dropped, recursively,
     * so Keycloak creates new groups rather than attempting to move groups by a now-deleted id.
     */
    fun forCreate(): OrganizationGroup =
        copy(id = null, path = null, subGroups = subGroups?.map { it.forCreate() })
}
