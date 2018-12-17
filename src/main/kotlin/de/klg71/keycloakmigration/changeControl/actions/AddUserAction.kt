package de.klg71.keycloakmigration.changeControl.actions

import de.klg71.keycloakmigration.changeControl.KeycloakException
import de.klg71.keycloakmigration.model.AddUser
import de.klg71.keycloakmigration.rest.isSuccessful
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class AddUserAction(
        private val realm: String,
        private val name: String,
        private val enabled: Boolean = true,
        private val emailVerified: Boolean = true,
        private val attributes: Map<String, List<String>> = mapOf()) : Action() {

    private lateinit var userUuid: UUID

    private val addUser = addUser()

    private fun addUser() = AddUser(name, enabled, emailVerified, attributes)

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(name)
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
            }!!

    override fun hash() = hash


    override fun execute() {
        client.addUser(addUser, realm).run {
            if (!isSuccessful()) {
                throw KeycloakException(this.body().asReader().readText())
            }
            headers()["location"]!!.stream().findFirst().get()
        }.run {
            split("/").last()
        }.let {
            userUuid = UUID.fromString(it)
        }
    }

    override fun undo() {
        client.deleteUser(userUuid, realm)
    }

    override fun name() = "AddUser"

}