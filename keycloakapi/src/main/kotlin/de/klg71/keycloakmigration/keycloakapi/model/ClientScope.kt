package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class ClientScope(
        val id: UUID,
        val name: String,
        val description: String?,
        val protocol: String,
        val attributes: Map<String, String>,
        val protocolMappers: List<ProtocolMapper>?)

data class ClientScopeItem(
        val id: UUID,
        val name: String
)
