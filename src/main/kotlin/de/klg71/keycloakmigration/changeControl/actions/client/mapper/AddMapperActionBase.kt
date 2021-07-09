package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.*
import de.klg71.keycloakmigration.keycloakapi.model.AddMapper
import java.util.UUID


internal fun addMapper(client: KeycloakClient, mapper: AddMapper, clientScopeName: String, name: String, realm: String): UUID {
    if (client.mapperExistsByName(clientScopeName, name, realm)) {
        throw MigrationException(
                "Mapper with name: $name already exists in client scope: $clientScopeName on realm: ${realm}!")
    }
    client.clientScopeUUID(clientScopeName, realm).let {
        client.addMapper(it, mapper, realm)
    }.run {
        return extractLocationUUID()
    }
}

internal fun addClientMapper(client: KeycloakClient, mapper: AddMapper, clientId: String, name: String, realm: String): UUID {
    if (client.clientMapperExistsByName(clientId, name, realm)) {
        throw MigrationException(
                "Mapper with name: $name already exists in client: $clientId on realm: ${realm}!")
    }
    client.clientUUID(clientId, realm).let {
        client.addClientMapper(it, mapper, realm)
    }.run {
        return extractLocationUUID()
    }
}

abstract class AddMapperActionBase(
        realm: String?,
        protected val name: String,
        protected val clientId: String? = null,
        protected val clientScopeName: String? = null,
): Action(realm) {
    protected lateinit var clientMapperUuid: UUID
    protected lateinit var mapperUuid: UUID

    /**
     * Creates the mapper that's going to be added to the client or client scope
     */
    protected abstract fun createMapper(): AddMapper

    /**
     * Asserts that one of the property `clientId` or `clientScopeName` is present
     */
    protected fun assertPrimaryKeyExists() {
        if (clientId == null && clientScopeName == null) {
            throw MigrationException(
                "Mapper with name: $name must have either clientId or clientScopeName set, but doesn't define one!");
        }
    }

    override fun execute() {
        assertPrimaryKeyExists();

        if (clientId != null) {
            clientMapperUuid = addClientMapper(client, createMapper(), clientId, name, realm())
        }
        if (clientScopeName != null) {
            mapperUuid = addMapper(client, createMapper(), clientScopeName, name, realm())
        }
    }

    override fun undo() {
        if (clientId != null) {
            client.clientUUID(clientId, realm()).let {
                client.deleteClientMapper(it, clientMapperUuid, realm())
            }
        }
        if (clientScopeName != null) {
            client.clientScopeUUID(clientScopeName, realm()).let {
                client.deleteMapper(it, mapperUuid, realm())
            }
        }
    }

    override fun name(): String {
        val mapper = createMapper()
        return "AddMapper ${mapper.protocol}:${mapper.protocolMapper} ${mapper.name} to ${clientId ?: clientScopeName}"
    }
}

internal class AddMapperAction(
        realm: String?,
        name: String,
        clientId: String? = null,
        clientScopeName: String? = null,
        private val config: Map<String, String>,
        private val protocolMapper: String,
        private val protocol: String = "openid-connect"
) : AddMapperActionBase(realm, name, clientId, clientScopeName) {

    override fun createMapper(): AddMapper {
        return AddMapper(name, config, protocol, protocolMapper);
    }

}
