package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class AssignClientScope(
        val client: UUID,
        val clientScopeId: UUID,
        val realm: String)

