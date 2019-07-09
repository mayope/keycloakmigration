package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.AssignRole
import de.klg71.keycloakmigration.model.Role
import de.klg71.keycloakmigration.rest.*
import org.apache.commons.codec.digest.DigestUtils
import java.util.Objects.isNull

class RevokeRoleFromGroupAction(
        private val realm: String,
        private val role: String,
        private val group: String,
        private val clientId: String? = null) : Action() {

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(role)
                append(group)
                append(client)
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        if (!client.existsGroup(group, realm)) {
            throw MigrationException("Group with name: $group does not exist in realm: $realm!")
        }
        if (!client.existsRole(role, realm)) {
            throw MigrationException("Role with name: $role does not exist in realm: $realm!")
        }

        client.groupRoles(realm, client.groupUUID(group, realm)).run {
            if (!map { it.name }.contains(role)) {
                throw MigrationException("Group with name: $group in realm: $realm does not have role: $role!")
            }
        }

        findRole().run {
            assignRole()
        }.let {
            if (clientId != null) {
                client.revokeClientRolesFromGroup(listOf(it), realm, client.groupUUID(group, realm), client.clientUUID(clientId, realm))
            } else {
                client.revokeRealmRolesFromGroup(listOf(it), realm, client.groupUUID(group, realm))
            }
        }

    }

    private fun Role.assignRole() = AssignRole(isNull(client), composite, containerId, id, name)

    override fun undo() {
        findRole().run {
            assignRole()
        }.let {
            if (clientId != null) {
                client.assignClientRolesToGroup(listOf(it), realm, client.groupUUID(group, realm), client.clientUUID(clientId, realm))
            } else {
                client.assignRealmRolesToGroup(listOf(it), realm, client.groupUUID(group, realm))
            }
        }
    }

    private fun findRole() = if (clientId == null) {
        client.roleByName(role, realm)
    } else {
        client.clientRoleByName(role, clientId, realm)
    }

    override fun name() = "AssignRole $role to Group: $group"

}