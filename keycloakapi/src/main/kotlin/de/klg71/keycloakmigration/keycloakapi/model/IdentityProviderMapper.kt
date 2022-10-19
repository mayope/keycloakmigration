package de.klg71.keycloakmigration.keycloakapi.model

data class IdentityProviderMapper(
    val id: String,
    val name: String,
    val identityProviderAlias: String,
    val identityProviderMapper: String,
    val config: Map<String, String>
)
