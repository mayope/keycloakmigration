package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig

/**
 * Refuses a change that couldn't be rolled back: Keycloak returned values of these [configs] masked, so
 * undo would recreate them with "**********" in place of the real values.
 */
internal fun refuseMaskedConfigs(change: String, configs: List<AuthenticatorConfig>) {
    if (configs.isEmpty()) return
    throw MigrationException(
        "$change can't be rolled back, so it isn't run: Keycloak hides values of the authenticator config " +
            "${configs.joinToString { "'${it.alias}'" }} (secrets, or keys the provider doesn't declare), and " +
            "a rollback would write \"**********\" in their place. Change the flow by hand instead."
    )
}
