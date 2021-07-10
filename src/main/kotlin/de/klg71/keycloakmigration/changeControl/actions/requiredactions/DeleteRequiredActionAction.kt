package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.importRequiredAction
import de.klg71.keycloakmigration.keycloakapi.model.RequiredActionProviderItem


class DeleteRequiredActionAction(
    realm: String?,
    private val alias: String
) : Action(realm) {

    private var oldRequiredAction: RequiredActionProviderItem? = null

    override fun execute() {
        client.requiredActions(realm())
                .firstOrNull { it.alias == alias }?.also {
                    oldRequiredAction = it
                }?.let {
                    client.deleteRequiredAction(realm(), alias)
                }

    }

    override fun undo() {
        oldRequiredAction?.let {
            client.importRequiredAction(realm(), RequiredActionProviderItem(
                    it.alias,
                    it.config,
                    it.defaultAction,
                    it.enabled,
                    it.name,
                    it.priority,
                    it.providerId
            ))
        }
    }

    override fun name() = "DeleteRequiredAction $alias"
}
