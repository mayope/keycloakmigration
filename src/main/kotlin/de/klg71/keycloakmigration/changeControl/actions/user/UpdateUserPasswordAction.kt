package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.model.UserCredential
import de.klg71.keycloakmigration.rest.existsUser
import de.klg71.keycloakmigration.rest.userByName
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.text.Charsets.UTF_8


const val HASH_ITERATIONS = 27500
// 64 Bytes
const val KEY_BIT_LENGTH = 64 * 8
const val RANDOM_SALT_LENGTH = 15

/**
 * Updates the users password to the given one,
 * WARNING: This action can't be undone
 */
class UpdateUserPasswordAction(
        realm: String? = null,
        private val name: String,
        private val password: String,
        private val salt: String? = null) : Action(realm) {

    lateinit var user: User


    private fun updateUser() = User(user.id, user.createdTimestamp,
            user.username,
            user.enabled,
            user.emailVerified,
            userAttributes(),
            user.notBefore,
            user.totp,
            user.access,
            user.disableableCredentialTypes,
            user.requiredActions,
            user.email,
            user.firstName,
            user.lastName,
            createCredentials())

    private fun createCredentials() = listOf(createCredential(salt ?: generateSalt()))

    private fun generateSalt() = randomAlphanumeric(RANDOM_SALT_LENGTH)

    private fun createCredential(salt: String): UserCredential {
        return UserCredential(
                hashedSaltedValue = getEncryptedPassword(password, salt.toByteArray(UTF_8)),
                salt = Base64.getEncoder().encodeToString(salt.toByteArray(UTF_8)),
                hashIterations = HASH_ITERATIONS
        )
    }

    private fun getEncryptedPassword(password: String, salt: ByteArray): String {
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").run {
            generateSecret(PBEKeySpec(password.toCharArray(), salt,
                    HASH_ITERATIONS, KEY_BIT_LENGTH)
            )
        }.run {
            @Suppress("UsePropertyAccessSyntax")
            getEncoded()
        }.let {
            Base64.getEncoder().encodeToString(it)
        }

    }


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

    override fun name() = "UpdateUserPassword $name"
}
