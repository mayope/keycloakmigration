package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddMapper
import de.klg71.keycloakmigration.keycloakapi.model.Mapper

// todo: remove `open` modifier when deprecation below gets removed!
open class DeleteClientMapperAction(
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
            addClientMapper(client, AddMapper.from(it), clientId, name, realm())
        }
    }

    override fun name() = "DeleteClientMapper $name from $clientId"

}

@Deprecated("Will be removed in a future release. Use DeleteClientMapperAction action instead")
class DeleteMapperAction(
    realm: String?, name: String, clientId: String
) : DeleteClientMapperAction(
    realm, name, clientId
) {

    override fun name() = "DeleteMapper $name from $clientId"

}