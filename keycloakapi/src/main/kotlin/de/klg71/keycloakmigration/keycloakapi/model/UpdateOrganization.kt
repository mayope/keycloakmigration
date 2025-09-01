package de.klg71.keycloakmigration.keycloakapi.model

data class UpdateOrganization(
    val alias: String,
    val name: String?,
    val redirectUrl: String?,
    val domains: Set<OrganizationDomain>? = setOf(),
    val attributes: Map<String, List<String>>? = mapOf()
)
