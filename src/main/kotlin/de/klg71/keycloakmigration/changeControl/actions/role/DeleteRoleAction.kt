package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.AddRole
import de.klg71.keycloakmigration.model.Role
import de.klg71.keycloakmigration.rest.clientRoleByName
import de.klg71.keycloakmigration.rest.clientUUID
import de.klg71.keycloakmigration.rest.roleExistsByName
import org.apache.commons.codec.digest.DigestUtils

class DeleteRoleAction(
        private val realm: String,
        private val name: String,
        private val clientId: String? = null) : Action() {

    private lateinit var deletedRole: Role

    private fun addRole() = AddRole(name, deletedRole.description)
    private fun updateRole() = Role(deletedRole.id, deletedRole.name, deletedRole.description,
            deletedRole.composite,
            deletedRole.clientRole,
            deletedRole.containerId,
            deletedRole.attributes)

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(name)
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        if(!client.roleExistsByName(name, realm)){
            throw MigrationException("Role with name: $name does not exist in realm: $realm!")
        }
        findRole().run {
            deletedRole = this
            client.deleteRole(id, realm)
        }
    }

    override fun undo() {
        if (clientId == null) {
            client.addRole(addRole(), realm)
        } else {
            client.addClientRole(addRole(), client.clientUUID(clientId, realm), realm)
        }
        findRole().run {
            client.updateRole(updateRole(), id, realm)
        }
    }

    private fun findRole() = if (clientId == null) {
        client.roleByName(name, realm)
    } else {
        client.clientRoleByName(name, clientId, realm)
    }

    override fun name() = "DeleteRole $name"

}