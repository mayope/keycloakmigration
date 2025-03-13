package de.klg71.keycloakmigration.keycloakapi.model;

data class AddOrganization(
    val name: String,
    val description: String,
    val domains: List<Domain>,
    val attributes: Map<String, String>
)
