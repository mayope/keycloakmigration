package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.model.UserAccess
import de.klg71.keycloakmigration.rest.existsUser
import de.klg71.keycloakmigration.rest.userByName
import org.apache.commons.codec.digest.DigestUtils

class UpdateUserAction(
        private val realm: String,
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
        private val lastName: String? = null) : Action() {

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
            lastName ?: user.lastName)

    private fun userAttributes(): Map<String, List<String>> = user.attributes ?: emptyMap()

    private val hash = calculateHash()

    private fun calculateHash() = StringBuilder().run {
        append(realm)
        append(name)
        append(enabled)
        append(emailVerified)
        append(access)
        disableableCredentialTypes?.forEach {
            append(it)
        }
        append(notBefore)
        append(firstName)
        append(lastName)

        toString()
    }.let {
        DigestUtils.sha256Hex(it)
    }!!

    override fun hash() = hash

    override fun execute() {
        if (!client.existsUser(name, realm)) {
            throw MigrationException("User with name: $name does not exist in realm: $realm!")
        }

        user = client.userByName(name, realm)
        client.updateUser(user.id, updateUser(), realm)
    }

    override fun undo() {
        client.userByName(user.username, realm).run {
            client.updateUser(id, user, realm)
        }
    }

    override fun name() = "UpdateUser $name"

}