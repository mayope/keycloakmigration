package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.importRequiredAction
import de.klg71.keycloakmigration.keycloakapi.model.RequiredActionProviderItem


@Suppress("LongParameterList")
class AddRequiredActionAction(
    realm: String?,
    private val providerId: String,
    private val alias: String,
    private val name: String,
    private val config: Map<String, String>? = null,
    private val defaultAction: Boolean = false,
    private val enabled: Boolean = true,
    private val priority: Int? = null,
) : Action(realm) {

    override fun execute() {
        client.importRequiredAction(
            realm(), RequiredActionProviderItem(
                alias, config, defaultAction, enabled, name, priority, providerId
            )
        )
    }

    override fun undo() {
        client.deleteRequiredAction(realm(), alias)
    }

    override fun name() = "AddRequiredAction $alias"
}
