package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.RegisterRequiredActionProvider


@Suppress("LongParameterList")
class RegisterRequiredActionAction(
    realm: String,
    private val providerId: String,
    private val name: String,
) : Action(realm) {
    override fun execute() {
        client.registerRequiredAction(realm(), RegisterRequiredActionProvider(
                providerId,
                name
        ))
    }

    override fun undo() {
    }

    override fun name() = "RegisterRequiredAction $providerId"
}
