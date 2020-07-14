package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.Client
import de.klg71.keycloakmigration.keycloakapi.clientById
import de.klg71.keycloakmigration.keycloakapi.existsClient

class UpdateClientAction(
        realm: String? = null,
        private val clientId: String,
        private val name: String? = null,
        private val description: String? = null,
        private val enabled: Boolean? = null,
        private val attributes: Map<String, String>? = null,
        private val protocol: String? = null,
        private val redirectUris: List<String>? = null,
        private val bearerOnly: Boolean? = null,
        private val directAccessGrantEnabled: Boolean? = null,
        private val implicitFlowEnabled: Boolean? = null,
        private val standardFlowEnabled: Boolean? = null,
        private val serviceAccountsEnabled: Boolean? = null,
        private val publicClient: Boolean? = null,
        private val adminUrl: String? = null,
        private val baseUrl: String? = null,
        private val rootUrl: String? = null,
        private val webOrigins: List<String>? = null,
        private val fullScopeAllowed: Boolean? = null) : Action(realm) {

    private lateinit var oldClient: Client

    @Suppress("ComplexMethod")
    private fun updateClient() = Client(oldClient.id,
            clientId,
            name ?: oldClient.name,
            description ?: oldClient.description,
            oldClient.surrogateAuthRequired,
            enabled ?: oldClient.enabled,
            oldClient.clientAuthenticatorType,
            oldClient.defaultRoles,
            redirectUris ?: oldClient.redirectUris,
            webOrigins ?: oldClient.webOrigins,
            oldClient.notBefore,
            bearerOnly ?: oldClient.bearerOnly,
            oldClient.consentRequired,
            standardFlowEnabled ?: oldClient.standardFlowEnabled,
            implicitFlowEnabled ?: oldClient.implicitFlowEnabled,
            directAccessGrantEnabled ?: oldClient.directAccessGrantsEnabled,
            serviceAccountsEnabled ?: oldClient.serviceAccountsEnabled,
            publicClient ?: oldClient.publicClient,
            oldClient.frontchannelLogout,
            protocol ?: oldClient.protocol,
            attributes ?: oldClient.attributes,
            oldClient.authenticationFlowBindingOverrides,
            fullScopeAllowed ?:oldClient.fullScopeAllowed,
            oldClient.nodeReRegistrationTimeout,
            oldClient.protocolMappers,
            oldClient.defaultClientScopes,
            oldClient.optionalClientScopes,
            oldClient.access,
            baseUrl ?: oldClient.baseUrl,
            adminUrl ?: oldClient.adminUrl,
            rootUrl ?: oldClient.rootUrl,
            oldClient.secret)

    override fun execute() {
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
