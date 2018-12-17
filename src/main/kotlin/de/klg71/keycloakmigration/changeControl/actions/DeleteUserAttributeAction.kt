package de.klg71.keycloakmigration.changeControl.actions

import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.rest.userByName
import org.apache.commons.codec.digest.DigestUtils

class DeleteUserAttributeAction(
        private val realm: String,
        private val name: String,
        private val attributeName: String) : Action() {

    private lateinit var user: User

    private fun updateUser() =
            userAttributes().toMutableMap().let {
                if ("migrations" !in it) {
                    it["migrations"] = mutableListOf()
                }
                it["migrations"] = it["migrations"]!!.toMutableList().apply {
                    add(hash())
                }
                if (attributeName !in it) {
                    throw MigrationException("Attribute $attributeName is not present on ${user.username}")
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
                        user.lastName)
            }

    private fun userAttributes(): Map<String, List<String>> = user.attributes ?: emptyMap()

    private val hash = calculateHash()

    private fun calculateHash() = StringBuilder().run {
        append(realm)
        append(name)
        append(attributeName)
        toString()
    }.let {
        DigestUtils.sha256Hex(it)
    }!!

    override fun hash() = hash

    override fun execute() {
        user = client.userByName(name, realm)
        client.updateUser(user.id, updateUser(), realm)
    }

    override fun undo() {
        client.updateUser(user.id, user, realm)
    }

    override fun name() = "DeleteUserAttribute"

}