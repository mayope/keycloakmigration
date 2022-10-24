package de.klg71.keycloakmigration.changeControl.actions.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.existsUser
import de.klg71.keycloakmigration.keycloakapi.model.ResetPassword
import de.klg71.keycloakmigration.keycloakapi.userByName


/**
 * Updates the users password to the given one,
 * WARNING: This action can't be undone
 */
internal class UpdateUserPasswordAction(
    realm: String? = null,
    private val name: String,
    private val password: String) : Action(realm) {


    override fun execute() {
        if (!client.existsUser(name, realm())) {
            throw MigrationException("User with name: $name does not exist in realm: ${realm()}!")
        }
        val user = client.userByName(name, realm())

        client.updateUserPassword(user.id, ResetPassword(password), realm())
    }

    override fun undo() {
        // Can't be undone
    }

    override fun name() = "UpdateUserPassword $name"
}
