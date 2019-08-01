package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AddUser
import de.klg71.keycloakmigration.rest.extractLocationUUID
import de.klg71.keycloakmigration.rest.userByName
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class AddUserAction(
        realm:String?=null,
        private val name: String,
        private val enabled: Boolean = true,
        private val emailVerified: Boolean = true,
        private val attributes: Map<String, List<String>> = mapOf()) : Action(realm) {

    private lateinit var userUuid: UUID

    private val addUser = addUser()

    private fun addUser() = AddUser(name, enabled, emailVerified, attributes)

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
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
        client.addUser(addUser, realm()).run {
            userUuid = extractLocationUUID()
        }
    }

    override fun undo() {
        client.userByName(name,realm()).run {
            client.deleteUser(id, realm())
        }
    }

    override fun name() = "AddUser $name"

}