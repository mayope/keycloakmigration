package de.klg71.keycloakmigration.changeControl.actions.clientscope

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientScopeByName
import de.klg71.keycloakmigration.keycloakapi.existsClientScope
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.model.addClientScope
import java.util.UUID

class AddClientScopeAction(
        realm: String?,
        private val name: String,
        private val description: String? = null,
        private val protocol: String = "openid-connect",
        private val consentScreenText: String? = null,
        private val displayOnConsentScreen: Boolean = false,
        private val guiOrder: Int? = null,
        private val includeInTokenScope: Boolean = true
) : Action(realm) {

    private lateinit var scopeUUID: UUID

    override fun execute() {
        if (client.existsClientScope(name, realm())) {
            throw MigrationException("ClientScope with name: $name already exists in realm: ${realm()}!")
        }
        client.addClientScope(realm(), addClientScope(name, description, protocol, consentScreenText,
                displayOnConsentScreen, guiOrder, includeInTokenScope)).run {
            scopeUUID = extractLocationUUID()
        }
    }

    override fun undo() {
        client.clientScopeByName(name, realm()).run {
            client.deleteClientScope(realm(), id)
        }
    }

    override fun name() = "AddClientScope $name"

}
