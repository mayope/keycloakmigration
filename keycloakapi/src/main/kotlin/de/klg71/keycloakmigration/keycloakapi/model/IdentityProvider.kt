package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID
import com.fasterxml.jackson.annotation.JsonProperty

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
    @field:JsonProperty(required = false)
    @Deprecated(
        message = "Field removed in Keycloak 26.5.0+. Kept for backward compatibility",
        level = DeprecationLevel.WARNING
    )
    val updateProfileFirstLoginMode: String = "on"
)

