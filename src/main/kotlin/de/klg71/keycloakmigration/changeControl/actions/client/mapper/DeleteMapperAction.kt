package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddMapper
import de.klg71.keycloakmigration.keycloakapi.model.Mapper


internal class DeleteMapperAction(
    realm: String?,
    private val clientId: String,
    private val name: String
) : Action(realm) {

    private var oldMapper: Mapper? = null

    override fun execute() {
        val clientUUID = client.clientUUID(clientId, realm())
        oldMapper = client.mappers(clientUUID, realm()).firstOrNull { it.name == name }

        oldMapper?.let {
            client.deleteMapper(clientUUID, it.id, realm())
        }

    }

    override fun undo() {
        oldMapper?.let {
            addMapper(client, AddMapper(it.name, it.config, it.protocol, it.protocolMapper), clientId, it.name, realm())
        }
    }

    override fun name() = "DeleteMapper $name for client: $clientId"
}
