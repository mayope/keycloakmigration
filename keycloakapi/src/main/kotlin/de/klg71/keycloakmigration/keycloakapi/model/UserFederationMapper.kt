package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class UserFederationMapper(
        val id: UUID,
        val name: String,
        val config: Map<String, List<String>>,
        val parentId: UUID,
        val providerId: String,
        val providerType: String
)

