package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AddRole
import de.klg71.keycloakmigration.model.Role
import org.apache.commons.codec.digest.DigestUtils

class AddRoleAction(
        private val realm: String,
        private val name: String,
        private val description: String = "",
        private val attributes: Map<String, List<String>>?,
        private val composite: Boolean?,
        private val clientRole: Boolean?,
        private val containerId: String?) : Action() {

    private lateinit var createdRole: Role

    private fun addRole() = AddRole(name, description)
    private fun updateRole() = Role(createdRole.id, createdRole.name, createdRole.description,
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
        client.addRole(addRole(), realm)
        createdRole = client.roleByName(name, realm)
        client.updateRole(updateRole(), createdRole.id, realm)
    }

    override fun undo() {
        client.deleteRole(createdRole.id, realm)
    }

    override fun name() = "AddRole $name"

}