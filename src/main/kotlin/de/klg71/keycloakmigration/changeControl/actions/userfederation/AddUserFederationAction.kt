package de.klg71.keycloakmigration.changeControl.actions.userfederation

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.AddUserFederation
import de.klg71.keycloakmigration.keycloakapi.model.constructUserFederationConfig
import de.klg71.keycloakmigration.keycloakapi.userFederationExistsByName

class AddUserFederationAction(
        realm: String? = null,
        private val name: String,
        private val config: Map<String, String>?,
        private val providerId: String,
        private val providerType: String = "org.keycloak.storage.UserStorageProvider") : Action(realm) {


    override fun execute() {
        if (client.userFederationExistsByName(name, realm())) {
            throw MigrationException("UserFederation with name: $name already exists in realm: ${realm()}!")
        }
        client.addUserFederation(addUserFederation(), realm())
    }

    private fun addUserFederation(): AddUserFederation = AddUserFederation(
            name,
            realm(),
            constructUserFederationConfig(config ?: HashMap()),
            providerId,
            providerType
    )

    override fun undo() {
        client.userFederations(realm()).find {
            it.name == name
        }?.let {
            client.deleteUserFederation(realm(), it.id)
        }
    }


    override fun name() = "AddUserFederationAction $name"

}
