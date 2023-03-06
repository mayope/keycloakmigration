package de.klg71.keycloakmigration.changeControl.actions.clientscope

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientScopeByName
import de.klg71.keycloakmigration.keycloakapi.existsClientScope
import de.klg71.keycloakmigration.keycloakapi.model.ClientScope
import de.klg71.keycloakmigration.keycloakapi.model.addClientScope

@Suppress("LongParameterList")
class DeleteClientScopeAction(
    realm: String?,
    private val name: String
) : Action(realm) {

    private lateinit var existingScope: ClientScope

    override fun execute() {
        if (!client.existsClientScope(name, realm())) {
            throw MigrationException("ClientScope with name: $name doesn't exist in realm: ${realm()}!")
        }
        existingScope = client.clientScopeByName(name, realm())

        client.clientScopeByName(name, realm()).run {
            client.deleteClientScope(realm(), id)
        }
    }

    override fun undo() {
        if (!client.existsClientScope(name, realm())) {
            client.addClientScope(
                realm(), addClientScope(
                    existingScope.name, existingScope.description,
                    existingScope.protocol, existingScope.protocolMappers, config = existingScope.attributes
                )
            )
        }
    }

    override fun name() = "DeleteClientScopeAction $name"

}
