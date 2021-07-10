package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class ClientScope(
        val id: UUID,
        val name: String,
        val description: String? = null,
        val protocol: String,
        val attributes: Map<String, String>,
        val protocolMappers: List<ProtocolMapper>? = null)

data class ClientScopeItem(
        val id: UUID,
        val name: String
)
