package de.klg71.keycloakmigration.changeControl.actions

import de.klg71.keycloakmigration.changeControl.KeycloakException
import de.klg71.keycloakmigration.model.AddUser
import org.apache.commons.codec.digest.DigestUtils
import java.util.*
import java.util.Objects.isNull

class AddUserAction(
        private val realm: String,
        private val name: String,
        enabled: Boolean = true,
        emailVerified: Boolean = true,
        attributes: Map<String, List<String>> = mapOf()) : Action() {

    private lateinit var userUuid: UUID

    private val hash = calculateHash(name, enabled, emailVerified, attributes)

    private val addUser = addUser(name, enabled, emailVerified, attributes)

    override fun isRequired(): Boolean =
            client.getUserByName(name, realm)
                    .run {
                        if (isNull(attributes)) {
                            return true
                        }
                        if (!attributes!!.contains("migrations")) {
                            return true
                        }
                        return !attributes["migrations"]!!.contains(hash)
                    }


    private fun addUser(username: String, enabled: Boolean, emailVerified: Boolean, attributes: Map<String, List<String>>) =
            attributes.toMutableMap().let {
                it["migrations"] = listOf(hash)
                AddUser(username, enabled, emailVerified, it)
            }

    private fun calculateHash(username: String, enabled: Boolean, emailVerified: Boolean, attributes: Map<String, List<String>>) =
            StringBuilder().run {
                append(username)
                append(enabled)
                append(emailVerified)
                for ((key, value) in attributes) {
                    append(key)
                    value.forEach {
                        append(it)
                    }
                }
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }


    override fun execute() {
        val location = client.addUser(addUser, realm).run {
            if (this.status() != 201) {
                throw KeycloakException(this.body().asReader().readText())
            }
            headers()["location"]!!.stream().findFirst().get()
        }
        userUuid = UUID.fromString(location.split('/').last())
    }

    override fun undo() {
        client.deleteUser(userUuid, realm)
    }

    override fun name() = "AddUser"

}