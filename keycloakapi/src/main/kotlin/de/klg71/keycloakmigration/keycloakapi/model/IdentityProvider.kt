package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class IdentityProvider(
    val providerId: String,
    val internalId: UUID,
    val alias: String,
    val displayName: String? = null,
    val enabled: Boolean,
    val config: Map<String, String>,
    val trustEmail: Boolean,
    val storeToken: Boolean,
    val linkOnly: Boolean,
    val firstBrokerLoginFlowAlias: String="",
val postBrokerLoginFlowAlias: String="",
    val updateProfileFirstLoginMode: String
)

