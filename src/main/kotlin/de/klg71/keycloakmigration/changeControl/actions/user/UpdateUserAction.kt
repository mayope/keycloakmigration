package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.User
import de.klg71.keycloakmigration.keycloakapi.model.UserAccess
import de.klg71.keycloakmigration.keycloakapi.model.UserCredential
import de.klg71.keycloakmigration.keycloakapi.existsUser
import de.klg71.keycloakmigration.keycloakapi.userByName

class UpdateUserAction(
        realm: String? = null,
        private val name: String,
        private val enabled: Boolean? = null,
        private val emailVerified: Boolean? = null,
        private val access: UserAccess? = null,
        private val notBefore: Long? = null,
        private val totp: Boolean? = null,
        private val disableableCredentialTypes: List<String>? = null,
        private val requiredActions: List<String>? = null,
        private val email: String? = null,
        private val firstName: String? = null,
        private val lastName: String? = null,
        private val credentials: List<UserCredential>? = null) : Action(realm) {

    lateinit var user: User


    private fun updateUser() = User(user.id, user.createdTimestamp,
            user.username,
            enabled ?: user.enabled,
            emailVerified ?: user.emailVerified,
            userAttributes(),
            notBefore ?: user.notBefore,
            totp ?: user.totp,
            access ?: user.access,
            disableableCredentialTypes ?: user.disableableCredentialTypes,
            requiredActions ?: user.requiredActions,
            email ?: user.email,
            firstName ?: user.firstName,
            lastName ?: user.lastName,
            credentials ?: user.credentials)

    private fun userAttributes(): Map<String, List<String>> = user.attributes ?: emptyMap()

    override fun execute() {
        if (!client.existsUser(name, realm())) {
            throw MigrationException("User with name: $name does not exist in realm: ${realm()}!")
        }

        user = client.userByName(name, realm())
        client.updateUser(user.id, updateUser(), realm())
    }

    override fun undo() {
        if (client.existsUser(name, realm())) {
            client.updateUser(user.id, user, realm())
        }
    }

    override fun name() = "UpdateUser $name"

}
