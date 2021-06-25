package de.klg71.keycloakmigration.keycloakapi.model

import java.util.*

//@JsonInclude(JsonInclude.Include.NON_NULL)
data class ClientListItem(
        val id: UUID,
        val clientId: String,
        val name: String?,
        val description: String?,
        val baseUrl: String?,
        val surrogateAuthRequired: Boolean,
        val enabled: Boolean,
        val clientAuthenticatorType: String,
        val defaultRole: Role?,
        val redirectUris: List<String>,
        val webOrigins: List<String>,
        val notBefore: Int,
        val bearerOnly: Boolean,
        val consentRequired: Boolean,
        val standardFlowEnabled: Boolean,
        val implicitFlowEnabled: Boolean,
        val directAccessGrantsEnabled: Boolean,
        val serviceAccountsEnabled: Boolean,
        val publicClient: Boolean,
        val frontchannelLogout: Boolean,
        val protocol: String?,
        val attributes: Map<String, String>,
        val authenticationFlowBindingOverrides: Map<String, List<String>>,
        val fullScopeAllowed: Boolean,
        val nodeReRegistrationTimeout: Int,
        val protocolMappers: List<ProtocolMapper>?,
        val defaultClientScopes: List<String>,
        val optionalClientScopes: List<String>,
        val access: ClientAccess,
        val adminUrl: String?,
        val rootUrl: String?)

