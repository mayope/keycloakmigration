package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.AddSimpleClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import java.util.*

class AddSimpleClientAction(
        realm: String?,
        private val clientId: String,
        private val enabled: Boolean = true,
        private val attributes: Map<String, String> = mapOf(),
        private val protocol: String = "openid-connect",
        private val secret: String? = null,
        private val publicClient: Boolean = true,
        private val redirectUris: List<String> = emptyList()) : Action(realm) {

    private lateinit var clientUuid: UUID

    private val addClient = addClient()

    private fun addClient() = AddSimpleClient(clientId, enabled, attributes, protocol, redirectUris, secret,
            publicClient)

    override fun execute() {
        client.addSimpleClient(addClient, realm()).run {
            clientUuid = extractLocationUUID()
        }
    }

    override fun undo() {
        client.clientById(clientId, realm()).run {
            client.deleteClient(id, realm())
        }
    }

    override fun name() = "AddSimpleClient $clientId"

}
