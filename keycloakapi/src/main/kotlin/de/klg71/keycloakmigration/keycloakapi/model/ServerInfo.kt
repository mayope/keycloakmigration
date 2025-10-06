package de.klg71.keycloakmigration.keycloakapi.model

data class PasswordPolicy(
    val id: String,
    val displayName: String,
    val configType: String? = null,
    val defaultValue: String? = null,
    val multipleSupported: Boolean
)

data class ServerInfo(
    val passwordPolicies: List<PasswordPolicy>
)
