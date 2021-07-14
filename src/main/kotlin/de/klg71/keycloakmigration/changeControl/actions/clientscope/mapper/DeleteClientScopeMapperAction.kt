package de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddMapper
import de.klg71.keycloakmigration.keycloakapi.model.Mapper

open class DeleteClientScopeMapperAction(
    realm: String?,
    private val clientScopeName: String,
    private val name: String
) : Action(realm) {

    private var deletedMapper: Mapper? = null;

    override fun execute() {
        val clientScopeUUID = client.clientScopeUUID(clientScopeName, realm())
        deletedMapper = client.clientScopeMappers(clientScopeUUID, realm()).firstOrNull { it.name == name }
        deletedMapper?.let {
            client.deleteClientScopeMapper(clientScopeUUID, it.id, realm())
        }
    }

    override fun undo() {
        deletedMapper?.let {
            addClientScopeMapper(client, AddMapper.from(it), clientScopeName, name, realm())
        }
    }

    override fun name() = "DeleteClientScopeMapper $name from $clientScopeName"

}

