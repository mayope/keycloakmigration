package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientById
import de.klg71.keycloakmigration.keycloakapi.clientRoleByName
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.existsClient
import de.klg71.keycloakmigration.keycloakapi.existsClientRole
import de.klg71.keycloakmigration.keycloakapi.existsRole
import de.klg71.keycloakmigration.keycloakapi.model.Role
import de.klg71.keycloakmigration.keycloakapi.model.RoleScopeMapping
import java.util.Objects.isNull

class DeleteRoleScopeMappingAction(
        realm: String? = null,
        private val role: String,
        private val clientId: String,
        private val roleClientId: String? = null) : Action(realm) {

    override fun execute() {
        if (!client.existsClient(clientId, realm())) {
            throw MigrationException("Client with name: $clientId does not exist in realm: ${realm()}!")
        }
        if (roleClientId == null) {
            if (!client.existsRole(role, realm())) {
                throw MigrationException("Role with name: $role does not exist in realm: ${realm()}!")
            }
        } else {
            if (!client.existsClientRole(role, realm(), roleClientId)) {
                throw MigrationException(
                        "Role with name: $role in client: $roleClientId does not exist in realm: ${realm()}!")
            }
        }

        findRole().run {
            roleScopeMapping()
        }.let {
            if (roleClientId != null) {
                client.deleteClientRoleScopeMapping(listOf(it), realm(), client.clientUUID(clientId, realm()),
                        client.clientUUID(roleClientId, realm()))
            } else {
                client.deleteRealmRoleScopeMapping(listOf(it), realm(), client.clientUUID(clientId, realm()))
            }
        }
    }

    private fun Role.roleScopeMapping() =
        RoleScopeMapping(id, name, description, composite, isNull(client), containerId)

    override fun undo() {
        findRole().run {
            roleScopeMapping()
        }.let {
            if (roleClientId != null) {
                client.addClientRoleScopeMapping(listOf(it), realm(), client.clientUUID(clientId, realm()),
                    client.clientUUID(roleClientId, realm()))
            } else {
                client.addRealmRoleScopeMapping(listOf(it), realm(), client.clientUUID(clientId, realm()))
            }
        }
    }

    private fun findRole() = if (roleClientId == null) {
        client.roleByName(role, realm())
    } else {
        client.clientRoleByName(role, roleClientId, realm())
    }

    override fun name() = "AssignRole $role in client: $roleClientId to Client ServiceAccount: $clientId"

}
