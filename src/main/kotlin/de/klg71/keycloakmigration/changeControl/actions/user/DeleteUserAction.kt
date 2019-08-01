package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AddUser
import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.rest.extractLocationUUID
import de.klg71.keycloakmigration.rest.userByName
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class DeleteUserAction(
        realm:String?=null,
        private val name: String) : Action(realm) {

    private lateinit var user: User

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(name)
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        user = client.userByName(name, realm())
        client.deleteUser(user.id, realm())
    }

    override fun undo() {
        client.addUser(addUser(), realm()).run {
            extractLocationUUID()
        }.let {
            client.updateUser(it, updateUser(it), realm())
        }
    }

    private fun addUser() = AddUser(user.username, user.enabled, user.emailVerified, user.attributes ?: emptyMap())
    private fun updateUser(userUUID: UUID) = User(userUUID, user.createdTimestamp, user.username, user.enabled,
            user.emailVerified, user.attributes, user.notBefore, user.totp, user.access,
            user.disableableCredentialTypes, user.requiredActions, user.email, user.firstName, user.lastName, null)

    override fun name() = "DeleteUser $name"

}