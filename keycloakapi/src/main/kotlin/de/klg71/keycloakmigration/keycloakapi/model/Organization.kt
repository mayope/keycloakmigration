package de.klg71.keycloakmigration.keycloakapi.model;

import java.util.UUID

data class Domain(
    val name: String,
    val verified: Boolean
)

data class Organization(
    val id: UUID,
    val name: String,
    val description: String,
    val domains: List<Domain>,
    val attributes: Map<String, String>
)
