package de.klg71.keycloakmigration.changeControl.actions.userfederation

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.AddUserFederation
import de.klg71.keycloakmigration.model.UserFederation

class DeleteUserFederationAction(
        realm: String? = null,
        private val name: String) : Action(realm) {

    private var oldUserFederation: UserFederation? = null


    override fun execute() {
        client.userFederations(realm()).find {
            it.name == name
        }.let {
            it ?: throw MigrationException("UserFederation with name: $name does not exist in realm: ${realm()}!")
        }.let {
            oldUserFederation = it
            client.deleteUserFederation(realm(), it.id)
        }
    }

    override fun undo() {
        (oldUserFederation ?: return).apply {
            client.addLdap(AddUserFederation(name, parentId, config,
                    providerId, providerType), realm())
        }
    }


    override fun name() = "DeleteUserFederation $name"

}