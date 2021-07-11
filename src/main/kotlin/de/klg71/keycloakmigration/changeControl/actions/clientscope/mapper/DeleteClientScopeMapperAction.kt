package de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddMapper
import de.klg71.keycloakmigration.keycloakapi.model.Mapper

open class DeleteClientScopeMapperAction(
    realm: String?,
    protected val name: String,
    protected val clientId: String
) : Action(realm) {

    private var deletedMapper: Mapper? = null;

    override fun execute() {
        val clientUUID = client.clientUUID(clientId, realm())
        deletedMapper = client.clientMappers(clientUUID, realm()).firstOrNull { it.name == name }
        deletedMapper?.let {
            client.deleteClientMapper(clientUUID, it.id, realm())
        }
    }

    override fun undo() {
        deletedMapper?.let {
            addClientScopeMapper(client, AddMapper.from(it), clientId, name, realm())
        }
    }

    override fun name() = "DeleteClientScopeMapper $name from $clientId"

}

