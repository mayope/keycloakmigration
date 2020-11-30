package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.User
import de.klg71.keycloakmigration.keycloakapi.model.UserCredential
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import org.apache.commons.lang3.RandomStringUtils


internal const val HASH_ITERATIONS = 27500

// 64 Bytes
internal const val KEY_BIT_LENGTH = 64 * 8
internal const val RANDOM_SALT_LENGTH = 15


fun KeycloakClient.updateUserPassword(name: String, password: String, realm: String, salt: String? = null) {

    if (!existsUser(name, realm)) {
        throw KeycloakApiException("User with name: $name does not exist in realm: $realm!")
    }

    userByName(name, realm).let {
        updateUser(it.id, updateUserPassword(it, password, salt), realm)
    }
}


private fun updateUserPassword(user: User, password: String, salt: String?) = User(user.id, user.createdTimestamp,
    user.username,
    user.enabled,
    user.emailVerified,
    userAttributes(user),
    user.notBefore,
    user.totp,
    user.access,
    user.disableableCredentialTypes,
    user.requiredActions,
    user.email,
    user.firstName,
    user.lastName,
    createCredentials(password, salt))

private fun userAttributes(user: User): Map<String, List<String>> = user.attributes ?: emptyMap()

private fun createCredentials(password: String, salt: String?) = listOf(
    createCredential(password, salt ?: generateSalt()))

private fun generateSalt() = RandomStringUtils.randomAlphanumeric(RANDOM_SALT_LENGTH)

private fun createCredential(password: String, salt: String): UserCredential {
    return UserCredential(
        hashedSaltedValue = getEncryptedPassword(password, salt.toByteArray(Charsets.UTF_8)),
        salt = Base64.getEncoder().encodeToString(salt.toByteArray(Charsets.UTF_8)),
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
