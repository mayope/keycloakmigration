package de.klg71.keycloakmigration.changeControl.actions.userfederation

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.AddUserFederation
import de.klg71.keycloakmigration.keycloakapi.model.constructAdLdapConfig
import de.klg71.keycloakmigration.keycloakapi.userFederationExistsByName

class AddAdLdapAction(
        realm: String? = null,
        private val name: String,
        private val config: Map<String, String>) : Action(realm) {


    override fun execute() {
        if (client.userFederationExistsByName(name, realm())) {
            throw MigrationException("UserFederation with name: $name already exists in realm: ${realm()}!")
        }
        client.addUserFederation(addLdap(), realm())
    }

    private fun addLdap(): AddUserFederation = AddUserFederation(name, realm(), constructAdLdapConfig(config))

    override fun undo() {
        client.userFederations(realm()).find {
            it.name == name
        }?.let {
            client.deleteUserFederation(realm(), it.id)
        }
    }


    override fun name() = "AddLdap $name"

}
