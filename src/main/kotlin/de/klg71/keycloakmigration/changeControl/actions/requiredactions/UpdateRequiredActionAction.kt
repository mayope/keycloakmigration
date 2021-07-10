package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.RequiredActionProviderItem


class UpdateRequiredActionAction(
    realm: String?,
    private val alias: String,
    private val providerId: String?,
    private val newAlias: String? = null,
    private val name: String?,
    private val config: Map<String, String>?,
    private val defaultAction: Boolean?,
    private val enabled: Boolean?,
    private val priority: Int?,
) : Action(realm) {

    private var oldRequiredAction: RequiredActionProviderItem? = null

    override fun execute() {
        client.requiredActions(realm())
                .firstOrNull { it.alias == alias }?.also {
                    oldRequiredAction = it
                }?.let {
                    client.updateRequiredAction(
                            realm(),it.alias, RequiredActionProviderItem(
                            newAlias ?: it.alias,
                            config ?: it.config,
                            defaultAction ?: it.defaultAction,
                            enabled ?: it.enabled,
                            name ?: it.name,
                            priority ?: it.priority,
                            providerId ?: it.providerId
                            )
                    )
                }

    }

    override fun undo() {
        val updateRequiredAction = oldRequiredAction ?: error("undo called but oldRequiredAction is null")

        client.requiredActions(realm())
                .firstOrNull { it.alias == newAlias ?: alias }?.let {
                    client.updateRequiredAction(
                            realm(), it.alias, RequiredActionProviderItem(
                                updateRequiredAction.alias,
                                updateRequiredAction.config,
                                updateRequiredAction.defaultAction,
                                updateRequiredAction.enabled,
                                updateRequiredAction.name,
                                updateRequiredAction.priority,
                                updateRequiredAction.providerId
                            )
                    )
                }
    }

    override fun name() = "UpdateRequiredAction $alias"
}
