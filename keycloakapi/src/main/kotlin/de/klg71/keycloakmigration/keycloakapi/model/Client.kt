package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class ClientAccess(val view: Boolean, val configure: Boolean, val manage: Boolean)

data class Client(
    val id: UUID,
    val clientId: String,
    val name: String?,
    val description: String?,
    val surrogateAuthRequired: Boolean,
    val enabled: Boolean,
    val alwaysDisplayInConsole: Boolean = false,
    val clientAuthenticatorType: String,
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
    val baseUrl: String?,
    val adminUrl: String?,
    val secret: String?,
    val rootUrl: String?
)

class UpdateClientBuilder(private val existingClient: Client) {

    var id: UUID = existingClient.id
    var clientId: String = existingClient.clientId
    var name: String? = existingClient.name
    var description: String? = existingClient.description
    var surrogateAuthRequired: Boolean = existingClient.surrogateAuthRequired
    var enabled: Boolean = existingClient.enabled
    var alwaysDisplayInConsole: Boolean = existingClient.alwaysDisplayInConsole
    var clientAuthenticatorType: String = existingClient.clientAuthenticatorType
    var redirectUris: List<String> = existingClient.redirectUris
    var webOrigins: List<String> = existingClient.webOrigins
    var notBefore: Int = existingClient.notBefore
    var bearerOnly: Boolean = existingClient.bearerOnly
    var consentRequired: Boolean = existingClient.consentRequired
    var standardFlowEnabled: Boolean = existingClient.standardFlowEnabled
    var implicitFlowEnabled: Boolean = existingClient.implicitFlowEnabled
    var directAccessGrantsEnabled: Boolean = existingClient.directAccessGrantsEnabled
    var serviceAccountsEnabled: Boolean = existingClient.serviceAccountsEnabled
    var publicClient: Boolean = existingClient.publicClient
    var frontchannelLogout: Boolean = existingClient.frontchannelLogout
    var protocol: String? = existingClient.protocol
    var attributes: Map<String, String> = existingClient.attributes
    var authenticationFlowBindingOverrides: Map<String, List<String>> = existingClient.authenticationFlowBindingOverrides
    var fullScopeAllowed: Boolean = existingClient.fullScopeAllowed
    var nodeReRegistrationTimeout: Int = existingClient.nodeReRegistrationTimeout
    var protocolMappers: List<ProtocolMapper>? = existingClient.protocolMappers
    var defaultClientScopes: List<String> = existingClient.defaultClientScopes
    var optionalClientScopes: List<String> = existingClient.optionalClientScopes
    var access: ClientAccess = existingClient.access
    var baseUrl: String? = existingClient.baseUrl
    var adminUrl: String? = existingClient.adminUrl
    var secret: String? = existingClient.secret
    var rootUrl: String? = existingClient.rootUrl

    fun build() = Client(
        id,
        clientId,
        name,
        description,
        surrogateAuthRequired,
        enabled,
        alwaysDisplayInConsole,
        clientAuthenticatorType,
        redirectUris,
        webOrigins,
        notBefore,
        bearerOnly,
        consentRequired,
        standardFlowEnabled,
        implicitFlowEnabled,
        directAccessGrantsEnabled,
        serviceAccountsEnabled,
        publicClient,
        frontchannelLogout,
        protocol,
        attributes,
        authenticationFlowBindingOverrides,
        fullScopeAllowed,
        nodeReRegistrationTimeout,
        protocolMappers,
        defaultClientScopes,
        optionalClientScopes,
        access,
        baseUrl,
        adminUrl,
        secret,
        rootUrl
    )

}
