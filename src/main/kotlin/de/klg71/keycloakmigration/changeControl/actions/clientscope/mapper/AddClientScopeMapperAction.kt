package de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientScopeMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddMapper
import java.util.*

internal fun addClientScopeMapper(
    client: KeycloakClient,
    mapper: AddMapper,
    clientScopeName: String,
    name: String,
    realm: String
): UUID {
    if (client.clientScopeMapperExistsByName(clientScopeName, name, realm)) {
        throw MigrationException(
            "Mapper with name: $name already exists in client scope: $clientScopeName on realm: ${realm}!"
        )
    }
    client.clientScopeUUID(clientScopeName, realm).let {
        client.addClientScopeMapper(it, mapper, realm)
    }.run {
        return extractLocationUUID()
    }
}

open class AddClientScopeMapperAction(
    realm: String?,
    protected val clientScopeName: String,
    protected val name: String,
    protected val config: Map<String, String> = mapOf(),
    protected val protocol: String = "openid-connect",
    protected val protocolMapper: String = ""
) : Action(realm) {

    private lateinit var mapperUuid: UUID

    protected open fun createMapper(): AddMapper = AddMapper(name, config, protocol, protocolMapper)

    override fun execute() {
        mapperUuid = addClientScopeMapper(client, createMapper(), clientScopeName, name, realm())
    }

    override fun undo() {
        client.clientScopeUUID(clientScopeName, realm()).let {
            client.deleteClientScopeMapper(it, mapperUuid, realm())
        }
    }

    override fun name(): String = "AddClientScopeMapper $protocol:$protocolMapper $name to $clientScopeName";

}
