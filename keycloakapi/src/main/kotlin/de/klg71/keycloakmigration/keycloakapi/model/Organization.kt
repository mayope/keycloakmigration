package de.klg71.keycloakmigration.keycloakapi.model;

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
