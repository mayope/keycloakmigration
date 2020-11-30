package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.updateUserPassword


/**
 * Updates the users password to the given one,
 * WARNING: This action can't be undone
 */
internal class UpdateUserPasswordAction(
    realm: String? = null,
    private val name: String,
    private val password: String,
    private val salt: String? = null) : Action(realm) {


    override fun execute() {
        client.updateUserPassword(name, password, realm(), salt)
    }

    override fun undo() {
        // Can't be undone
    }

    override fun name() = "UpdateUserPassword $name"
}
