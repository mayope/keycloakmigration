package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientById
import de.klg71.keycloakmigration.keycloakapi.existsClient
import de.klg71.keycloakmigration.keycloakapi.model.Client

@Suppress("LongParameterList")
class UpdateClientAction(
    realm: String? = null,
    private val clientId: String,
    private val name: String? = null,
    private val description: String? = null,
    private val surrogateAuthRequired: Boolean? = null,
    private val enabled: Boolean? = null,
    private val alwaysDisplayInConsole: Boolean? = null,
    private val clientAuthenticatorType: String? = null,
    private val attributes: Map<String, String>? = null,
    private val protocol: String? = null,
    private val redirectUris: List<String>? = null,
    private val notBefore: Int? = null,
    private val bearerOnly: Boolean? = null,
    private val consentRequired: Boolean? = null,
    private val directAccessGrantEnabled: Boolean? = null,
    private val implicitFlowEnabled: Boolean? = null,
    private val standardFlowEnabled: Boolean? = null,
    private val serviceAccountsEnabled: Boolean? = null,
    private val publicClient: Boolean? = null,
    private val frontchannelLogout: Boolean? = null,
    private val adminUrl: String? = null,
    private val baseUrl: String? = null,
    private val rootUrl: String? = null,
    private val webOrigins: List<String>? = null,
    private val fullScopeAllowed: Boolean? = null,
    private val nodeReRegistrationTimeout: Int ?= null) : Action(realm) {

    companion object {
      @JvmStatic
      val supportedClientAuthenticatorTypes = listOf(
          "client-jwt", "client-secret", "client-secret-jwt", "client-x509"
      )
    }

    private lateinit var oldClient: Client

    @Suppress("ComplexMethod")
    private fun updateClient() = Client(
        oldClient.id,
        clientId,
        name ?: oldClient.name,
        description ?: oldClient.description,
        surrogateAuthRequired ?: oldClient.surrogateAuthRequired,
        enabled ?: oldClient.enabled,
        alwaysDisplayInConsole ?: oldClient.alwaysDisplayInConsole,
        clientAuthenticatorType ?: oldClient.clientAuthenticatorType,
        redirectUris ?: oldClient.redirectUris,
        webOrigins ?: oldClient.webOrigins,
        notBefore ?: oldClient.notBefore,
        bearerOnly ?: oldClient.bearerOnly,
        consentRequired ?: oldClient.consentRequired,
        standardFlowEnabled ?: oldClient.standardFlowEnabled,
        implicitFlowEnabled ?: oldClient.implicitFlowEnabled,
        directAccessGrantEnabled ?: oldClient.directAccessGrantsEnabled,
        serviceAccountsEnabled ?: oldClient.serviceAccountsEnabled,
        publicClient ?: oldClient.publicClient,
        frontchannelLogout ?: oldClient.frontchannelLogout,
        protocol ?: oldClient.protocol,
        attributes ?: oldClient.attributes,
        oldClient.authenticationFlowBindingOverrides,
        fullScopeAllowed ?: oldClient.fullScopeAllowed,
        nodeReRegistrationTimeout ?: oldClient.nodeReRegistrationTimeout,
        oldClient.protocolMappers,
        oldClient.defaultClientScopes,
        oldClient.optionalClientScopes,
        oldClient.access,
        baseUrl ?: oldClient.baseUrl,
        adminUrl ?: oldClient.adminUrl,
        oldClient.secret,
        rootUrl ?: oldClient.rootUrl
    )

    override fun execute() {
        if (clientAuthenticatorType != null && clientAuthenticatorType !in supportedClientAuthenticatorTypes) {
            throw MigrationException(
                "Client authenticator type '$clientAuthenticatorType' is not supported. " +
                "Use one of: ${supportedClientAuthenticatorTypes.joinToString(", ")}"
            )
        }

        if (!client.existsClient(clientId, realm())) {
            throw MigrationException("Client with id: $clientId does not exist in realm: $realm!")
        }

        oldClient = client.clientById(clientId, realm())
        client.updateClient(oldClient.id, updateClient(), realm())
    }

    override fun undo() {
        if (client.existsClient(clientId, realm())) {
            client.updateClient(oldClient.id, oldClient, realm())
        }
    }

    override fun name() = "UpdateClient $clientId"
}
