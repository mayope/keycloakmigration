package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class RoleScopeMapping(
    val id: UUID,
    val name: String, // role name
    val description: String?,
    val composite: Boolean,
    val clientRole: Boolean,
    val containerId: String // REALM
)
