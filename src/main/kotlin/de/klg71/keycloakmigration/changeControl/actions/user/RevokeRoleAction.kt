package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AssignRole
import de.klg71.keycloakmigration.model.Role
import de.klg71.keycloakmigration.rest.clientRoleByName
import de.klg71.keycloakmigration.rest.clientUUID
import de.klg71.keycloakmigration.rest.userUUID
import org.apache.commons.codec.digest.DigestUtils
import java.util.Objects.isNull

class RevokeRoleAction(
        private val realm: String,
        private val role: String,
        private val user: String,
        private val clientId: String?) : Action() {

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


    private fun Role.assignRole() = AssignRole(isNull(client), composite, containerId, id, name)

    override fun undo() {
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

    private fun findRole() = if (clientId == null) {
        client.roleByName(role, realm)
    } else {
        client.clientRoleByName(role, clientId, realm)
    }


    override fun name() = "RevokeRole $role from $user"

}