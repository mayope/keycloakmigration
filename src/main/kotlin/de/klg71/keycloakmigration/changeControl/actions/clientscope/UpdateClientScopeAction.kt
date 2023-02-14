package de.klg71.keycloakmigration.changeControl.actions.clientscope

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientScopeByName
import de.klg71.keycloakmigration.keycloakapi.existsClientScope
import de.klg71.keycloakmigration.keycloakapi.model.ClientScope
import de.klg71.keycloakmigration.keycloakapi.model.ProtocolMapper
import de.klg71.keycloakmigration.keycloakapi.model.updateClientScope

@Suppress("LongParameterList")
class UpdateClientScopeAction(
        realm: String?,
        private val name: String,
        private val description: String? = null,
        private val protocol: String = "openid-connect",
        private val protocolMappers: List<ProtocolMapper>? = listOf(),
        private val consentScreenText: String? = null,
        private val displayOnConsentScreen: Boolean = false,
        private val guiOrder: Int? = null,
        private val includeInTokenScope: Boolean = true,
        private val config: Map<String, String>? = null
) : Action(realm) {

    private lateinit var existingScope: ClientScope

    override fun execute() {
        if (!client.existsClientScope(name, realm())) {
            throw MigrationException("ClientScope with name: $name doesn't exist in realm: ${realm()}!")
        }
        existingScope = client.clientScopeByName(name, realm())

        client.updateClientScope(realm(), existingScope.id, updateClientScope(name, description, protocol, protocolMappers, consentScreenText,
                displayOnConsentScreen, guiOrder, includeInTokenScope, config))
    }

    override fun undo() {
        client.updateClientScope(realm(), existingScope.id, updateClientScope(existingScope.name, existingScope.description,
                existingScope.protocol, existingScope.protocolMappers, config=existingScope.attributes))
    }

    override fun name() = "UpdateClientScope $name"

}
