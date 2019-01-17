package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.AssignRole
import de.klg71.keycloakmigration.model.Role
import de.klg71.keycloakmigration.rest.*
import org.apache.commons.codec.digest.DigestUtils
import java.util.Objects.isNull

class AssignRoleAction(
        private val realm: String,
        private val role: String,
        private val user: String,
        private val clientId: String? = null) : Action() {

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(role)
                append(client)
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        if(!client.existsUser(user,realm)){
            throw MigrationException("User with name: $user does not exist in realm: $realm!")
        }
        if(!client.existsRole(role,realm)){
            throw MigrationException("Role with name: $role does not exist in realm: $realm!")
        }

        findRole().run {
            assignRole()
        }.let {
            if (clientId != null) {
                client.assignClientRoles(listOf(it), realm, client.userUUID(user, realm), client.clientUUID(clientId, realm))
            } else {
                client.assignRealmRoles(listOf(it), realm, client.userUUID(user, realm))
            }
        }
    }

    private fun Role.assignRole() = AssignRole(isNull(client), composite, containerId, id, name)

    override fun undo() {
        findRole().run {
            assignRole()
        }.let {
            if (clientId != null) {
                client.revokeClientRoles(listOf(it), realm, client.userUUID(user, realm), client.clientUUID(clientId, realm))
            } else {
                client.revokeRealmRoles(listOf(it), realm, client.userUUID(user, realm))
            }
        }
    }

    private fun findRole() = if (clientId == null) {
        client.roleByName(role, realm)
    } else {
        client.clientRoleByName(role, clientId, realm)
    }

    override fun name() = "AssignRole $role to $user"

}