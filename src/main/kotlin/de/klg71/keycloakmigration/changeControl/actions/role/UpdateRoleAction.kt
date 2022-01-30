package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.clientRoleByName
import de.klg71.keycloakmigration.keycloakapi.existsClient
import de.klg71.keycloakmigration.keycloakapi.model.Role
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import de.klg71.keycloakmigration.keycloakapi.roleExistsByName

private const val HTTP_NO_CONTENT = 204

@Suppress("LongParameterList")
class UpdateRoleAction(
    realm: String? = null,
    private val name: String,
    private val clientId: String? = null,
    private val description: String? = null,
    private val attributes: Map<String, List<String>>? = null,
    private val composite: Boolean? = null,
    private val clientRole: Boolean? = null,
    private val containerId: String? = null,
    private val compositeChildRoles: List<RoleSelector>? = null
) : Action(realm) {

    private lateinit var originalRole: Role
    private fun updateRole() = Role(
        originalRole.id,
        originalRole.name,
        description ?: originalRole.description,
        composite ?: originalRole.composite,
        clientRole ?: originalRole.clientRole,
        containerId ?: originalRole.containerId,
        attributes ?: originalRole.attributes,
    )


    override fun execute() {
        if (clientId != null) {
            if (!client.existsClient(clientId, realm())) {
                throw MigrationException("Client with id: $clientId does not exist in realm: ${realm()}!")
            }
            if (!client.roleExistsByName(name, realm(), clientId)) {
                throw MigrationException("ClientRole with name: $name does not exist in realm: ${realm()}!")
            }
        } else {
            if (!client.roleExistsByName(name, realm())) {
                throw MigrationException("Role with name: $name does not exist in realm: ${realm()}!")
            }
        }

        originalRole = findRole()


        // If the action fails from here we have to roll it back
        client.updateRole(updateRole(), originalRole.id, realm())
        if (composite == true && !compositeChildRoles.isNullOrEmpty()) {
            val roleItems = compositeChildRoles.map(this::findRoleAsRoleListItem)
            val response = client.addCompositeToRole(roleItems, originalRole.id, realm())
            if (response.status() != HTTP_NO_CONTENT) {
                throw KeycloakApiException("addCompositeToRole failed. response: $response")
            }
        }
    }

    override fun undo() {
        if (client.roleExistsByName(name, realm())) {
            client.updateRole(originalRole, originalRole.id, realm())
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
            it.id, it.name, it.description, it.composite, it.clientRole, it.containerId
        )
    }

    override fun name() = "UpdateRole $name"
}
