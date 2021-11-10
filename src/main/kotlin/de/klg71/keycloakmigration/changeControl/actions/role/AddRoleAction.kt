package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.clientRoleByName
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddRole
import de.klg71.keycloakmigration.keycloakapi.model.Role
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import de.klg71.keycloakmigration.keycloakapi.roleExistsByName

data class RoleSelector(val name: String,
    val clientId: String? = null)

private const val HTTP_NO_CONTENT = 204

@Suppress("LongParameterList")
class AddRoleAction(
    realm: String? = null,
    private val name: String,
    private val clientId: String? = null,
    private val description: String = "",
    private val attributes: Map<String, List<String>>? = null,
    private val composite: Boolean? = null,
    private val clientRole: Boolean? = null,
    private val containerId: String? = null,
    private val compositeChildRoles: List<RoleSelector>? = null) : Action(realm) {

    private fun addRole() = AddRole(name, description)
    private fun updateRole(createdRole: Role) = Role(
        createdRole.id, createdRole.name, createdRole.description,
        composite ?: createdRole.composite,
        clientRole ?: createdRole.clientRole,
        containerId ?: createdRole.containerId,
        attributes()
    )

    private fun attributes(): Map<String, List<String>> = attributes ?: emptyMap()

    override fun execute() {
        if (client.roleExistsByName(name, realm())) {
            throw MigrationException("Role with name: $name already exists in realm: ${realm()}!")
        }
        if (clientId == null) {
            client.addRole(addRole(), realm())
        } else {
            client.addClientRole(addRole(), client.clientUUID(clientId, realm()), realm())
        }
        // If the action fails from here we have to role it back
        setExecuted()
        val role = findRole()
        client.updateRole(updateRole(role), role.id, realm())
        if (composite == true && !compositeChildRoles.isNullOrEmpty()) {
            val roleItems = compositeChildRoles.map(this::findRoleAsRoleListItem)
            val response = client.addCompositeToRole(roleItems, role.id, realm())
            if (response.status() != HTTP_NO_CONTENT) {
                throw KeycloakApiException("addCompositeToRole failed. response: $response")
            }
        }
    }

    override fun undo() {
        findRole().run {
            client.deleteRole(id, realm())
        }
    }

    private fun findRole() = findRole(RoleSelector(name = name, clientId = clientId))

    private fun findRole(selector: RoleSelector) = if (selector.clientId == null) {
        client.roleByName(selector.name, realm())
    } else {
        client.clientRoleByName(selector.name, selector.clientId, realm())
    }

    private fun findRoleAsRoleListItem(selector: RoleSelector) = findRole(selector).let {
        RoleListItem(
            it.id, it.name, it.description,
            it.composite, it.clientRole, it.containerId
        )
    }

    override fun name() = "AddRole $name"
}
