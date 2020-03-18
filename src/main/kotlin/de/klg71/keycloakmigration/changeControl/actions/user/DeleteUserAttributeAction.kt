package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.rest.userByName

class DeleteUserAttributeAction(
        realm: String? = null,
        private val name: String,
        private val attributeName: String,
        private val failOnMissing: Boolean = true) : Action(realm) {

    private lateinit var user: User

    private fun updateUser() =
            userAttributes().toMutableMap().let {
                if (attributeName !in it && failOnMissing) {
                    throw MigrationException("Attribute $attributeName is not present on user ${user.username}!")
                }
                it.remove(attributeName)
                User(user.id, user.createdTimestamp,
                        user.username,
                        user.enabled,
                        user.emailVerified,
                        it,
                        user.notBefore,
                        user.totp,
                        user.access,
                        user.disableableCredentialTypes,
                        user.requiredActions,
                        user.email,
                        user.firstName,
                        user.lastName, null)
            }

    private fun userAttributes(): Map<String, List<String>> = user.attributes ?: emptyMap()

    override fun execute() {
        user = client.userByName(name, realm())
        client.updateUser(user.id, updateUser(), realm())
    }

    override fun undo() {
        client.updateUser(user.id, user, realm())
    }

    override fun name() = "DeleteUserAttribute $name"

}
