package de.klg71.keycloakmigration.changeControl.actions.clientscope

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.existsClientRole
import de.klg71.keycloakmigration.keycloakapi.existsClientScope
import de.klg71.keycloakmigration.keycloakapi.clientRoleByName
import de.klg71.keycloakmigration.keycloakapi.existsRole
import de.klg71.keycloakmigration.keycloakapi.model.AssignRole
import de.klg71.keycloakmigration.keycloakapi.model.Role
import java.util.Objects.isNull

class AssignRoleToClientScopeAction(
    realm: String? = null, private val name: String, private val role: String, private val roleClientId: String? = null
) : Action(realm) {
    override fun execute() {
        if (!client.existsClientScope(name, realm())) {
            throw MigrationException(
                "ClientScope with name: $name does not exist in realm: ${realm()}!"
            )
        }
        if (roleClientId == null) {
            if (!client.existsRole(role, realm())) {
                throw MigrationException(
                    "Role with name: $role does not exist in realm: ${realm()}!"
                )
            }
        } else {
            if (!client.existsClientRole(role, realm(), roleClientId)) {
                throw MigrationException(
                    "Role with name: $role in client: $roleClientId does not exist in realm: ${realm()}!"
                )
            }
        }

        findRole().run { assignRole() }.let {
            val clientScopeUUID = client.clientScopeUUID(name, realm())

            if (roleClientId != null) {
                val clientUUID = client.clientUUID(roleClientId, realm())

                client.assignClientRoleToClientScope(
                    listOf(it), realm(), clientScopeUUID, clientUUID
                )
            } else {
                client.assignRealmRoleToClientScope(listOf(it), realm(), clientScopeUUID)
            }
        }
    }

    private fun Role.assignRole() = AssignRole(isNull(client), composite, containerId, id, name)

    override fun undo() {
        findRole().run { assignRole() }.let {
            val clientScopeUUID = client.clientScopeUUID(name, realm())

            if (roleClientId != null) {
                val clientUUID = client.clientUUID(roleClientId, realm())
                client.revokeClientRoleFromClientScope(
                    listOf(it), realm(), clientScopeUUID, clientUUID
                )
            } else {
                client.revokeRealmRoleFromClientScope(listOf(it), realm(), clientScopeUUID)
            }
        }
    }

    private fun findRole() = if (roleClientId == null) {
        client.roleByName(role, realm())
    } else {
        client.clientRoleByName(role, roleClientId, realm())
    }

    override fun name() = "AssignRole $role to ClientScope: $name"
}
