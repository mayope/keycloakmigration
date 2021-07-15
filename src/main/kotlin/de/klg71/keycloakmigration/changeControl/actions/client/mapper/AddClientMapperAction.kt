package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddMapper
import java.util.*

internal fun addClientMapper(
    client: KeycloakClient,
    mapper: AddMapper,
    clientId: String,
    name: String,
    realm: String
): UUID {
    if (client.clientMapperExistsByName(clientId, name, realm)) {
        throw MigrationException(
            "Mapper with name: $name already exists in client: $clientId on realm: ${realm}!"
        )
    }
    client.clientUUID(clientId, realm).let {
        client.addClientMapper(it, mapper, realm)
    }.run {
        return extractLocationUUID()
    }
}

open class AddClientMapperAction(
    realm: String?,
    protected val clientId: String,
    protected val name: String,
    protected val config: Map<String, String> = mapOf(),
    protected val protocol: String = "openid-connect",
    protected val protocolMapper: String = ""
) : Action(realm) {

    private lateinit var mapperUuid: UUID

    protected open fun createMapper(): AddMapper = AddMapper(name, config, protocol, protocolMapper)

    override fun execute() {
        mapperUuid = addClientMapper(client, createMapper(), clientId, name, realm())
    }

    override fun undo() {
        client.clientUUID(clientId, realm()).let {
            client.deleteClientMapper(it, mapperUuid, realm())
        }
    }

    override fun name(): String = "AddClientMapper $protocol:$protocolMapper $name to $clientId";

}

// todo(@janunld): @klg71 do we want to keep deprecated classes anyway? don't know if there might be any outer
//  dependents in terms of actual java/kotlin code. currently all previously available `AddMapper*Action` are
//  still there so potential dependents don't loose anything when "upgrading"
@Deprecated(
    "Will be removed in a future release. Use AddClientMapper action instead",
    ReplaceWith(
        "AddClientMapperAction(realm, clientId, name, config, protocol, protocolMapper)",
        "de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddClientMapper"
    )
)
class AddMapperAction(
    realm: String?,
    clientId: String,
    name: String,
    config: Map<String, String> = mapOf(),
    protocol: String = "openid-connect",
    protocolMapper: String = ""
) : AddClientMapperAction(
    realm, clientId, name, config, protocol, protocolMapper
) {

    override fun name(): String = "AddMapper $protocol:$protocolMapper $name to $clientId";

}

