package de.klg71.keycloakmigration.changeControl.actions

import de.klg71.keycloakmigration.KeycloakClient
import de.klg71.keycloakmigration.changeControl.Action
import de.klg71.keycloakmigration.changeControl.KeycloakException
import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.model.UserAccess
import org.apache.commons.codec.digest.DigestUtils
import java.util.Objects.isNull

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
        private val lastName: String?) : Action {

    private lateinit var user: User
    private val hash = calculateHash()

    override fun isRequired(client: KeycloakClient): Boolean {
        user = client.searchByUsername(name, realm)
                .run {
                    if (isEmpty()) {
                        throw MigrationException("User with name: $name does not exist in $realm")
                    }
                    first()
                }
                .run {
                    client.user(id, realm)
                }

        user.run {
            if (isNull(attributes)) {
                return true
            }
            if (!attributes!!.contains("migrations")) {
                return true
            }
            return !attributes["migrations"]!!.contains(hash)
        }
    }

    private fun updateUser() =
            userAttributes().toMutableMap().let {
                if ("migrations" !in it) {
                    it["migrations"] = mutableListOf()
                }
                it["migrations"] = it["migrations"]!!.toMutableList().apply {
                    add(calculateHash())
                }
                User(user.id, user.createdTimestamp,
                        user.username,
                        enabled ?: user.enabled,
                        emailVerified ?: user.emailVerified,
                        it,
                        notBefore ?: user.notBefore,
                        totp ?: user.totp,
                        access ?: user.access,
                        disableableCredentialTypes ?: user.disableableCredentialTypes,
                        requiredActions ?: user.requiredActions,
                        email ?: user.email,
                        firstName ?: user.firstName,
                        lastName ?: user.lastName)
            }

    private fun userAttributes(): Map<String, List<String>> = user.attributes ?: emptyMap()

    private fun calculateHash() =
            StringBuilder().run {
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
            }

    private var executed: Boolean = false

    override fun execute(client: KeycloakClient) {
        client.updateUser(user.id, updateUser(), realm).run {
            if (this.status() != 204) {
                throw KeycloakException(this.body().asReader().readText())
            }
        }
        executed = true
    }

    override fun undo(client: KeycloakClient) {
        if (executed) {
            client.updateUser(user.id, user, realm)
        }
    }

    override fun name() = "UpdateUser"

}