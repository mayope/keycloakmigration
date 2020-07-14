package de.klg71.keycloakmigration.keycloakapi.model

data class RoleScopeMapping(
    val id: String,
    val name: String, // role name
    val description: String,
    val composite: Boolean,
    val clientRole: Boolean,
    val containerId: String // REALM
)
