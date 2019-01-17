package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AddRole
import de.klg71.keycloakmigration.model.Role
import de.klg71.keycloakmigration.rest.clientRoleByName
import de.klg71.keycloakmigration.rest.clientUUID
import de.klg71.keycloakmigration.rest.existsUser
import org.apache.commons.codec.digest.DigestUtils

class AddRoleAction(
        private val realm: String,
        private val name: String,
        private val clientId: String? = null,
        private val description: String = "",
        private val attributes: Map<String, List<String>>? = null,
        private val composite: Boolean? = null,
        private val clientRole: Boolean? = null,
        private val containerId: String? = null) : Action() {

    private fun addRole() = AddRole(name, description)
    private fun updateRole(createdRole: Role) = Role(createdRole.id, createdRole.name, createdRole.description,
            composite ?: createdRole.composite,
            clientRole ?: createdRole.clientRole,
            containerId ?: createdRole.containerId,
            attributes())

    private fun attributes(): Map<String, List<String>> = attributes ?: emptyMap()

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(name)
                append(description)
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        if (clientId == null) {
            client.addRole(addRole(), realm)
        } else {
            client.addClientRole(addRole(), client.clientUUID(clientId, realm), realm)
        }
        findRole().run {
            client.updateRole(updateRole(this), id, realm)
        }
    }

    override fun undo() {
        findRole().run {
            client.deleteRole(id, realm)
        }
    }

    private fun findRole() = if (clientId == null) {
        client.roleByName(name, realm)
    } else {
        client.clientRoleByName(name, clientId, realm)
    }

    override fun name() = "AddRole $name"

}