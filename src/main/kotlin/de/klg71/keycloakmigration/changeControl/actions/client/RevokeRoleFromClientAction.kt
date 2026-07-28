package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientRoleByName
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.existsClient
import de.klg71.keycloakmigration.keycloakapi.existsClientRole
import de.klg71.keycloakmigration.keycloakapi.existsRole
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import de.klg71.keycloakmigration.keycloakapi.model.AssignRole
import de.klg71.keycloakmigration.keycloakapi.userRoles

class RevokeRoleFromClientAction(
    realm: String?,
    private val role: String,
    private val clientId: String,
    private val roleClientId: String? = null
) : Action(realm) {

    private lateinit var roleListItem: RoleListItem

    override fun execute() {
        roleListItem = findRole().let {
            RoleListItem(
                id = it.id,
                name = it.name,
                description = it.description,
                composite = it.composite,
                clientRole = it.clientRole,
                containerId = it.containerId
            )
        }

        if (!client.existsClient(clientId, realm()))
            throw MigrationException("Client: $clientId does not exist in realm: ${realm()}!")

        val serviceAccountUser = client.clientServiceAccount(client.clientUUID(clientId, realm()), realm())

        if (roleClientId != null) {
            if (!client.existsClientRole(role, realm(), roleClientId))
                throw MigrationException("Client role: $role does not exist " +
                    "for client '$roleClientId' in realm: ${realm()}!")

            val clientOfRoleUUID = client.clientUUID(roleClientId, realm())
            val assignedClientRolesToServiceAccount = client.userClientRoles(
                realm(),
                serviceAccountUser.id,
                clientOfRoleUUID
            )

            if (!assignedClientRolesToServiceAccount.map { it.name }.contains(role))
                throw MigrationException(
                    "Client '$clientId' in realm: ${realm()} does not have client role: $roleClientId.$role!"
                )

            client.revokeClientRoles(
                listOf(roleListItem.toAssignRole()),
                realm(),
                serviceAccountUser.id,
                clientOfRoleUUID
            )

        } else {
            if (!client.existsRole(role, realm()))
                throw MigrationException("Realm role: $role does not exist in realm: ${realm()}!")

            val userRoles = client.userRoles(realm(), serviceAccountUser.id, false)

            if (!userRoles.contains(roleListItem))
                throw MigrationException(
                    "Client '$clientId' in realm: ${realm()} does not have realm role: $role!"
                )

            client.revokeRealmRoles(
                listOf(roleListItem.toAssignRole()),
                realm(),
                serviceAccountUser.id
            )
        }
    }

    override fun undo() {
        if (roleClientId != null) {
            val serviceAccountUser = client.clientServiceAccount(client.clientUUID(clientId, realm()), realm())

            client.assignClientRoles(
                listOf(roleListItem.toAssignRole()),
                realm(),
                serviceAccountUser.id,
                client.clientUUID(roleClientId, realm())
            )
        } else {
            client.assignRealmRoles(
                listOf(roleListItem.toAssignRole()),
                realm(),
                client.clientUUID(clientId, realm())
            )
        }
    }

    private fun findRole() = if (roleClientId != null) {
        client.clientRoleByName(role, roleClientId, realm())
    } else {
        client.roleByName(role, realm())
    }

    private fun RoleListItem.toAssignRole(): AssignRole {
        return AssignRole(
            id = this.id,
            name = this.name,
            composite = this.composite,
            clientRole = this.clientRole,
            containerId = this.containerId
        )
    }

    override fun name() = "Revoke Role $role from Client: $clientId"
}
