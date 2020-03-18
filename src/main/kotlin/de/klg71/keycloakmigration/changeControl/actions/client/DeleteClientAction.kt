package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AddClient
import de.klg71.keycloakmigration.model.Client
import de.klg71.keycloakmigration.rest.clientById

class DeleteClientAction(
        realm: String? = null,
        private val clientId: String) : Action(realm) {

    lateinit var clientRepresentation: Client

    override fun execute() {
        client.clientById(clientId, realm()).let {
            clientRepresentation = client.client(it.id, realm())
            client.deleteClient(it.id, realm())
        }

    }

    override fun undo() {
        client.addClient(addClient(), realm())
    }

    private fun addClient(): AddClient =
            clientRepresentation.run {
                AddClient(
                        clientId,
                        name,
                        description,
                        baseUrl,
                        surrogateAuthRequired,
                        enabled,
                        clientAuthenticatorType,
                        defaultRoles,
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
                        adminUrl,
                        rootUrl)
            }


    override fun name() = "DeleteClient $clientId"

}
