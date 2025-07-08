package de.klg71.keycloakmigration.keycloakapi.model

data class AddOrganization(
    val name: String,
    val alias: String?,
    val redirectUrl: String?,
    val domains: Set<OrganizationDomain>?,
    val attributes: Map<String, List<String>>? = mapOf()
)
