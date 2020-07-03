package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.AssignRole
import de.klg71.keycloakmigration.keycloakapi.model.Role
import de.klg71.keycloakmigration.keycloakapi.clientRoleByName
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.existsRole
import de.klg71.keycloakmigration.keycloakapi.existsUser
import de.klg71.keycloakmigration.keycloakapi.userUUID
import java.util.Objects.isNull

class RevokeRoleAction(
        realm: String? = null,
        private val role: String,
        private val user: String,
        private val clientId: String? = null) : Action(realm) {

    override fun execute() {
        if (!client.existsUser(user, realm())) {
            throw MigrationException("User with name: $user does not exist in realm: ${realm()}!")
        }
        if (!client.existsRole(role, realm())) {
            throw MigrationException("Role with name: $role does not exist in realm: ${realm()}!")
        }
        client.userRoles(realm(), client.userUUID(user, realm())).run {
            if (!map { it.name }.contains(role)) {
                throw MigrationException("User with name: $user in realm: ${realm()} does not have role: $role!")
            }
        }
        findRole().run {
            assignRole()
        }.let {
            if (clientId != null) {
                client.revokeClientRoles(listOf(it), realm(), client.userUUID(user, realm()),
                        client.clientUUID(clientId, realm()))
            } else {
                client.revokeRealmRoles(listOf(it), realm(), client.userUUID(user, realm()))
            }
        }
    }


    private fun Role.assignRole() = AssignRole(isNull(client), composite, containerId, id, name)

    override fun undo() {
        findRole().run {
            assignRole()
        }.let {
            if (clientId != null) {
                client.assignClientRoles(listOf(it), realm(), client.userUUID(user, realm()),
                        client.clientUUID(clientId, realm()))
            } else {
                client.assignRealmRoles(listOf(it), realm(), client.userUUID(user, realm()))
            }
        }
    }

    private fun findRole() = if (clientId == null) {
        client.roleByName(role, realm())
    } else {
        client.clientRoleByName(role, clientId, realm())
    }


    override fun name() = "RevokeRole $role from $user"

}
