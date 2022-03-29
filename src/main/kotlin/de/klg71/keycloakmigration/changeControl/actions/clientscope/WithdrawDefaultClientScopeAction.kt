package de.klg71.keycloakmigration.changeControl.actions.clientscope

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.existsClient
import de.klg71.keycloakmigration.keycloakapi.existsClientScope
import de.klg71.keycloakmigration.keycloakapi.model.AssignClientScope

class WithdrawDefaultClientScopeAction(
        realm: String?,
        private val clientScopeName: String,
        private val clientId: String
) : Action(realm) {

    override fun execute() {
        if (!client.existsClientScope(clientScopeName, realm())) {
            throw MigrationException("ClientScope with name: $clientScopeName does not exist in realm: ${realm()}!")
        }
        if (!client.existsClient(clientId, realm())) {
            throw MigrationException("Client with id: $clientId does not exist in realm: ${realm()}!")
        }
        val clientUUID = client.clientUUID(clientId, realm())
        val clientScopeUUID = client.clientScopeUUID(clientScopeName, realm())

        client.withdrawDefaultClientScope(realm(), clientUUID, clientScopeUUID)
    }

    override fun undo() {
        val clientUUID = client.clientUUID(clientId, realm())
        val clientScopeUUID = client.clientScopeUUID(clientScopeName, realm())
        client.assignDefaultClientScope(realm(), clientUUID,
            clientScopeUUID, AssignClientScope(clientUUID, clientScopeUUID, realm()))
    }

    override fun name() = "WithdrawDefaultClientScope $clientScopeName to client: $clientId"

}
