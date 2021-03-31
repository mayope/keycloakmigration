package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddUser
import de.klg71.keycloakmigration.keycloakapi.model.User
import de.klg71.keycloakmigration.keycloakapi.userByName
import org.slf4j.LoggerFactory
import java.util.UUID

class DeleteUserAction(
    realm: String? = null,
    private val name: String) : Action(realm) {
    private val logger = LoggerFactory.getLogger(DeleteUserAction::class.java)
    private lateinit var user: User

    override fun execute() {
        user = client.userByName(name, realm())
        client.deleteUser(user.id, realm())
    }

    override fun undo() {
        logger.warn("Readded deleted user: {}, you have to reset the password for this user", name)
        client.addUser(addUser(), realm()).run {
            extractLocationUUID()
        }.let {
            client.updateUser(it, updateUser(it), realm())
        }
    }

    private fun addUser() = AddUser(user.username, user.enabled, user.emailVerified, user.attributes ?: emptyMap())
    private fun updateUser(userUUID: UUID) = User(
        userUUID, user.createdTimestamp, user.username, user.enabled,
        user.emailVerified, user.attributes, user.notBefore, user.totp, user.access,
        user.disableableCredentialTypes, user.requiredActions, user.email, user.firstName, user.lastName, null
    )

    override fun name() = "DeleteUser $name"

}
