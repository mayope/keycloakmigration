package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.RegisterRequiredActionRequest
import de.klg71.keycloakmigration.keycloakapi.model.RequiredActionProviderItem
import java.nio.charset.Charset


class RegisterRequiredActionAction(
    realm: String?,
    private val name: String,
    private val providerId: String
) : Action(realm) {


    override fun execute() {
        client.registerRequiredAction(realm(), RegisterRequiredActionRequest(providerId, name)).let {
            if (!it.isSuccessful()) {
                throw MigrationException(
                    "Could not register required action with name: $name and providerId: $providerId body: ${
                        it.body().asReader(
                            Charset.defaultCharset()
                        ).readText()
                    }"
                )
            }
        }
    }

    override fun undo() {
        //Only deactivate required action to conform with keycloak-gui
        // There is no alias returned when registering the action, but it seems to be the same as the providerId?
        client.requiredActions(realm())
            .firstOrNull { it.alias == providerId }?.let {
                client.updateRequiredAction(
                    realm(), it.alias, RequiredActionProviderItem(
                        it.alias,
                        it.config,
                        it.defaultAction,
                        false,
                        it.name,
                        it.priority,
                        it.providerId
                    )
                )
            }

    }

    override fun name() = "RegisterRequiredAction $name $providerId"
}
