package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.AddMapper
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.clientUUID
import de.klg71.keycloakmigration.rest.extractLocationUUID
import de.klg71.keycloakmigration.rest.mapperExistsByName
import java.util.UUID


fun addMapper(client: KeycloakClient, mapper: AddMapper, clientId: String, name: String, realm: String): UUID {
    if (client.mapperExistsByName(clientId, name, realm)) {
        throw MigrationException(
                "Mapper with name: $name already exists in client: $clientId on realm: ${realm}!")
    }
    client.clientUUID(clientId, realm).let {
        client.addMapper(it, mapper, realm)
    }.run {
        return extractLocationUUID()
    }
}

class AddMapperAction(
        realm: String?,
        private val clientId: String,
        private val name: String,
        private val config: Map<String, String>,
        private val protocolMapper: String,
        private val protocol: String = "openid-connect"
) : Action(realm) {

    private lateinit var mapperUuid: UUID

    override fun execute() {
        mapperUuid = addMapper(client, AddMapper(name, config, protocol, protocolMapper), clientId, name, realm())
    }

    override fun undo() {
        client.clientUUID(clientId, realm()).let {
            client.deleteMapper(it, mapperUuid, realm())
        }
    }

    override fun name() = "AddMapper $clientId"

}
