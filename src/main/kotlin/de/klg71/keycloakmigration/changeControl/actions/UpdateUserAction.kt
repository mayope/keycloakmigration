package de.klg71.keycloakmigration.changeControl.actions

import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.model.UserAccess
import de.klg71.keycloakmigration.rest.userByName
import org.apache.commons.codec.digest.DigestUtils

class MigrationException(message: String) : RuntimeException(message)

class UpdateUserAction(
        private val realm: String,
        private val name: String,
        private val enabled: Boolean?,
        private val emailVerified: Boolean?,
        private val access: UserAccess?,
        private val notBefore: Long?,
        private val totp: Boolean?,
        private val disableableCredentialTypes: List<String>?,
        private val requiredActions: List<String>?,
        private val email: String?,
        private val firstName: String?,
        private val lastName: String?) : Action() {

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
        user = client.userByName(name,realm)
        client.updateUser(user.id, updateUser(), realm)
    }

    override fun undo() {
        client.updateUser(user.id, user, realm)
    }

    override fun name() = "UpdateUser"

}