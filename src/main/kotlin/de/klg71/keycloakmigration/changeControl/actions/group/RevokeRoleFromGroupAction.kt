package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.AssignRole
import de.klg71.keycloakmigration.keycloakapi.model.Role
import de.klg71.keycloakmigration.keycloakapi.clientRoleByName
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.existsGroup
import de.klg71.keycloakmigration.keycloakapi.existsRole
import de.klg71.keycloakmigration.keycloakapi.existsClientRole
import de.klg71.keycloakmigration.keycloakapi.groupUUID
import java.util.Objects.isNull

class RevokeRoleFromGroupAction(
    realm: String? = null,
    private val role: String,
    private val group: String,
    private val clientId: String? = null) : Action(realm) {

    override fun execute() {
        if (!client.existsGroup(group, realm())) {
            throw MigrationException("Group with name: $group does not exist in realm: ${realm()}!")
        }

        if (clientId == null) {
            if (!client.existsRole(role, realm())) {
                throw MigrationException("Role with name: $role does not exist in realm: ${realm()}!")
            }
        } else {
            if (!client.existsClientRole(role, realm(), clientId)) {
                throw MigrationException(
                    "Role with name: $role in client: $clientId" +
                            " does not exist in realm: ${realm()}!"
                )
            }
        }

        client.groupRoles(realm(), client.groupUUID(group, realm())).run {
            if (!map { it.name }.contains(role)) {
                throw MigrationException("Group with name: $group in realm: ${realm()} does not have role: $role!")
            }
        }

        findRole().run {
            assignRole()
        }.let {
            if (clientId != null) {
                client.revokeClientRolesFromGroup(
                    listOf(it), realm(), client.groupUUID(group, realm()),
                    client.clientUUID(clientId, realm())
                )
            } else {
                client.revokeRealmRolesFromGroup(listOf(it), realm(), client.groupUUID(group, realm()))
            }
        }

    }

    private fun Role.assignRole() = AssignRole(isNull(client), composite, containerId, id, name)

    override fun undo() {
        findRole().run {
            assignRole()
        }.let {
            if (clientId != null) {
                client.assignClientRolesToGroup(
                    listOf(it), realm(), client.groupUUID(group, realm()),
                    client.clientUUID(clientId, realm())
                )
            } else {
                client.assignRealmRolesToGroup(listOf(it), realm(), client.groupUUID(group, realm()))
            }
        }
    }

    private fun findRole() = if (clientId == null) {
        client.roleByName(role, realm())
    } else {
        client.clientRoleByName(role, clientId, realm())
    }

    override fun name() = "AssignRole $role to Group: $group"

}
