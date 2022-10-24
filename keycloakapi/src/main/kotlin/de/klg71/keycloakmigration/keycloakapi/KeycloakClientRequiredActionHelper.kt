@file:Suppress("TooManyFunctions")

package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.RegisterRequiredActionProvider
import de.klg71.keycloakmigration.keycloakapi.model.RequiredActionProviderItem

fun KeycloakClient.importRequiredAction(realm: String, requiredActionProviderItem: RequiredActionProviderItem) {
    if (requiredActions(realm).any { it.alias == requiredActionProviderItem.alias }) {
        throw KeycloakApiException(
            "Import RequiredAction failed, RequiredAction: ${requiredActionProviderItem.alias} already exists"
        )
    }

    val createdAction = requiredActions(realm).first { it.name == requiredActionProviderItem.name }

    updateRequiredAction(
        realm, createdAction.alias, RequiredActionProviderItem(
            requiredActionProviderItem.alias,
            requiredActionProviderItem.config,
            requiredActionProviderItem.defaultAction,
            requiredActionProviderItem.enabled,
            requiredActionProviderItem.name,
            requiredActionProviderItem.priority ?: createdAction.priority,
            requiredActionProviderItem.providerId
        )
    )
}
