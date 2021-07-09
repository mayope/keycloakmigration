package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddMapper
import de.klg71.keycloakmigration.keycloakapi.model.Mapper


internal class DeleteMapperAction(
    realm: String?,
    private val name: String,
    private val clientId: String? = null,
    private val clientScopeName: String? = null,
) : Action(realm) {
    private var deletedClientMapper: Mapper? = null;
    private var deletedMapper: Mapper? = null;

    override fun execute() {
        if (clientId == null && clientScopeName == null) {
            throw MigrationException(
                "Unable to delete mapper. Neither a client nor client scope is given, nothing to delete!");
        }

        if (clientId != null) {
            val clientUUID = client.clientUUID(clientId, realm())
            deletedClientMapper = client.clientMappers(clientUUID, realm()).firstOrNull { it.name == name }
            deletedClientMapper?.let {
                client.deleteClientMapper(clientUUID, it.id, realm())
            }
        }
        if (clientScopeName != null) {
            val clientScopeUUID = client.clientScopeUUID(clientScopeName, realm())
            deletedMapper = client.mappers(clientScopeUUID, realm()).firstOrNull { it.name == name }
            deletedMapper?.let {
                client.deleteMapper(clientScopeUUID, it.id, realm())
            }
        }

    }

    override fun undo() {
        if (clientId != null) {
            deletedClientMapper?.let {
                addClientMapper(client, AddMapper.from(it), clientId, name, realm())
            }
        }
        if (clientScopeName != null) {
            deletedMapper?.let {
                addMapper(client, AddMapper.from(it), clientScopeName, name, realm())
            }
        }
    }

    override fun name() = "DeleteMapper $name from ${clientId ?: clientScopeName}"
}
