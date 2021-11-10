package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.AddUser
import de.klg71.keycloakmigration.keycloakapi.model.AssignGroup
import de.klg71.keycloakmigration.keycloakapi.model.AssignRole
import de.klg71.keycloakmigration.keycloakapi.model.Role
import de.klg71.keycloakmigration.keycloakapi.model.User
import de.klg71.keycloakmigration.keycloakapi.clientRoleByName
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.groupUUID
import de.klg71.keycloakmigration.keycloakapi.userByName
import java.util.Objects
import java.util.UUID


data class ClientRole(val role: String, val client: String)

@Suppress("LongParameterList")
class AddUserAction(
        realm: String? = null,
        private val name: String,
        private val enabled: Boolean = true,
        private val emailVerified: Boolean = true,
        private val attributes: Map<String, List<String>> = mapOf(),
        private val realmRoles: List<String> = listOf(),
        private val clientRoles: List<ClientRole> = listOf(),
        private val groups: List<String> = listOf(),
        private val email: String? = null,
        private val firstName: String? = null,
        private val lastName: String? = null) : Action(realm) {

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
        updateUser()
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
            client.assignClientRoles(listOf(it.second), realm(), userUuid, client.clientUUID(it.first, realm()))
        }
    }

    private fun updateUser() {
        if (email == null && firstName == null && lastName == null) {
            return
        }
        val user = client.userByName(name, realm())
        val updatedUser = User(user.id, user.createdTimestamp, user.username, enabled,
                emailVerified, userAttributes(user), user.notBefore, user.totp,
                user.access, user.disableableCredentialTypes, user.requiredActions,
                email ?: user.email,
                firstName ?: user.firstName,
                lastName ?: user.lastName,
                user.credentials)
        client.updateUser(user.id, updatedUser, realm())
    }

    private fun userAttributes(user: User): Map<String, List<String>> = user.attributes ?: emptyMap()

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
