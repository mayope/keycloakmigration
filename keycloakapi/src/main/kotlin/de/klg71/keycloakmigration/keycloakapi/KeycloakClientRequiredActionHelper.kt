@file:Suppress("TooManyFunctions")

package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.RegisterRequiredActionProvider
import de.klg71.keycloakmigration.keycloakapi.model.RequiredActionProviderItem

fun KeycloakClient.importRequiredAction(realm: String, requiredActionProviderItem: RequiredActionProviderItem) {
    registerRequiredAction(realm, RegisterRequiredActionProvider(
            requiredActionProviderItem.providerId, requiredActionProviderItem.name
    ))

    val currentAlias = requiredActions(realm).first { it.name == requiredActionProviderItem.name }.alias

    updateRequiredAction(realm, currentAlias, RequiredActionProviderItem(
            requiredActionProviderItem.alias,
            requiredActionProviderItem.config,
            requiredActionProviderItem.defaultAction,
            requiredActionProviderItem.enabled,
            requiredActionProviderItem.name,
            requiredActionProviderItem.priority,
            requiredActionProviderItem.providerId
    ))
}
