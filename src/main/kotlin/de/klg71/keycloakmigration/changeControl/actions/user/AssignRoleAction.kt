package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AssignRole
import de.klg71.keycloakmigration.model.Role
import de.klg71.keycloakmigration.rest.clientById
import de.klg71.keycloakmigration.rest.clientRoleByName
import de.klg71.keycloakmigration.rest.userByName
import org.apache.commons.codec.digest.DigestUtils
import java.util.Objects.isNull

class AssignRoleAction(
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
                client.assignClientRoles(listOf(it), realm, userUUID(), clientUUID(clientId))
            } else {
                client.assignRealmRoles(listOf(it), realm, userUUID())
            }
        }
    }

    private fun clientUUID(clientId: String) = client.clientById(clientId, realm).id

    private fun Role.assignRole() = AssignRole(isNull(client), composite, containerId, id, name)

    override fun undo() {
        findRole().run {
            assignRole()
        }.let {
            if (clientId != null) {
                client.removeClientRoles(listOf(it), realm, userUUID(), clientUUID(clientId))
            } else {
                client.removeRealmRoles(listOf(it), realm, userUUID())
            }
        }
    }

    private fun findRole() = if (clientId == null) {
        client.roleByName(role, realm)
    } else {
        client.clientRoleByName(role, clientId, realm)
    }

    private fun userUUID() = client.userByName(user, realm).id

    override fun name() = "AssignRole $role to $user"

}