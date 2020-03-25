package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AddUser
import de.klg71.keycloakmigration.model.AssignGroup
import de.klg71.keycloakmigration.model.AssignRole
import de.klg71.keycloakmigration.model.Role
import de.klg71.keycloakmigration.rest.clientRoleByName
import de.klg71.keycloakmigration.rest.clientUUID
import de.klg71.keycloakmigration.rest.extractLocationUUID
import de.klg71.keycloakmigration.rest.groupUUID
import de.klg71.keycloakmigration.rest.userByName
import java.util.*


data class ClientRole(val role: String, val client: String)

class AddUserAction(
        realm: String? = null,
        private val name: String,
        private val enabled: Boolean = true,
        private val emailVerified: Boolean = true,
        private val attributes: Map<String, List<String>> = mapOf(),
        private val realmRoles: List<String> = listOf(),
        private val clientRoles: List<ClientRole> = listOf(),
        private val groups: List<String> = listOf()) : Action(realm) {

    private lateinit var userUuid: UUID

    private val addUser = addUser()

    private fun addUser() = AddUser(name, enabled, emailVerified, attributes)

    override fun execute() {
        client.addUser(addUser, realm()).run {
            userUuid = extractLocationUUID()
        }
        // If the action fails now the user has to be deleted
        setExecuted()
        assignGroups()
        assignRealmRoles()
        assignClientRoles()
    }

    private fun assignRealmRoles() {
        realmRoles.map {
            client.roleByName(it, realm())
        }.map {
            it.assignRole()
        }.let {
            client.assignRealmRoles(it, realm(), userUuid)
        }
    }

    private fun assignClientRoles() {
        clientRoles.map {
            it.client to client.clientRoleByName(it.role, it.client, realm())
        }.map {
            it.first to it.second.assignRole()
        }.map {
            client.assignClientRoles(listOf(it.second), realm(), userUuid, client.clientUUID(it.first,realm()))
        }
    }

    private fun Role.assignRole() = AssignRole(Objects.isNull(client), composite, containerId, id, name)

    private fun assignGroups() {
        groups.forEach {
            val groupUUID = client.groupUUID(it, realm())
            client.assignGroup(AssignGroup(realm(), groupUUID, userUuid), realm(), userUuid, groupUUID)
        }
    }

    override fun undo() {
        client.userByName(name, realm()).run {
            client.deleteUser(id, realm())
        }
    }

    override fun name() = "AddUser $name"

}
