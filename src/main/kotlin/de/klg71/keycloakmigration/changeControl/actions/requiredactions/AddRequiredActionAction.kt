package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.importFlow
import de.klg71.keycloakmigration.keycloakapi.importRequiredAction
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.ImportFlow
import de.klg71.keycloakmigration.keycloakapi.model.RegisterRequiredActionProvider
import de.klg71.keycloakmigration.keycloakapi.model.RequiredActionProviderItem
import java.util.UUID


class AddRequiredActionAction(
    realm: String?,
    private val providerId: String,
    private val alias: String,
    private val name: String,
    private val config: Map<String, String>?,
    private val defaultAction: Boolean,
    private val enabled: Boolean,
    private val priority: Int,
) : Action(realm) {

    override fun execute() {
        client.importRequiredAction(realm(), RequiredActionProviderItem(
                alias, config, defaultAction, enabled, name, priority, providerId
        ))
    }

    override fun undo() {
        client.deleteRequiredAction(realm(), alias)
    }

    override fun name() = "AddRequiredAction $alias"
}
