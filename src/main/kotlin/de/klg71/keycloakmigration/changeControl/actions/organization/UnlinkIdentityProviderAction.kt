package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.identityProviderByAlias
import de.klg71.keycloakmigration.keycloakapi.model.UpdateIdentityProvider
import de.klg71.keycloakmigration.keycloakapi.organizationByAlias
import de.klg71.keycloakmigration.keycloakapi.realmExistsById

class UnlinkIdentityProviderAction(
    realm: String?,
    private val organizationAlias: String,
    private val identityProviderAlias: String
) : Action(realm) {

    private lateinit var org: de.klg71.keycloakmigration.keycloakapi.model.Organization
    private lateinit var idp: de.klg71.keycloakmigration.keycloakapi.model.IdentityProvider
    private lateinit var originalConfig: Map<String, String>

    override fun execute() {
        if (!client.realmExistsById(realm()))
            throw MigrationException("Realm with id: ${realm()} does not exist!")

        try {
            org = client.organizationByAlias(organizationAlias, realm())
        } catch (e: Exception) {
            throw MigrationException("Organization with alias: $organizationAlias does not exist in realm: ${realm()}!")
        }

        try {
            idp = client.identityProviderByAlias(identityProviderAlias, realm())
        } catch (e: Exception) {
            throw MigrationException("Identity Provider with alias: $identityProviderAlias does not exist in realm: ${realm()}!")
        }

        // Check if this IDP is actually linked to this organization
        if (idp.config["organizationId"] != org.id.toString()) {
            throw MigrationException(
                "Identity Provider $identityProviderAlias is not linked to organization $organizationAlias!"
            )
        }

        originalConfig = idp.config.toMutableMap()

        // Remove organizationId from config
        val newConfig = idp.config.toMutableMap()
        newConfig.remove("organizationId")
        newConfig.remove("domain")
        newConfig.remove("hideOnLoginPage")
        newConfig.remove("redirectWhenEmailDomainMatches")

        val updateRequest = UpdateIdentityProvider(
            internalId = idp.internalId,
            providerId = idp.providerId,
            alias = idp.alias,
            displayName = idp.displayName,
            enabled = idp.enabled,
            config = newConfig,
            trustEmail = idp.trustEmail,
            storeToken = idp.storeToken,
            linkOnly = idp.linkOnly,
            firstBrokerLoginFlowAlias = idp.firstBrokerLoginFlowAlias,
            postBrokerLoginFlowAlias = idp.postBrokerLoginFlowAlias,
            updateProfileFirstLoginMode = idp.updateProfileFirstLoginMode
        )

        client.updateIdentityProvider(updateRequest, realm(), idp.alias)
    }

    override fun undo() {
        try {
            client.identityProviderByAlias(identityProviderAlias, realm())
        } catch (e: Exception) {
            throw MigrationException("Identity Provider with alias: $identityProviderAlias does not exist in realm: ${realm()}!")
        }

        val updateRequest = UpdateIdentityProvider(
            internalId = idp.internalId,
            providerId = idp.providerId,
            alias = idp.alias,
            displayName = idp.displayName,
            enabled = idp.enabled,
            config = originalConfig,
            trustEmail = idp.trustEmail,
            storeToken = idp.storeToken,
            linkOnly = idp.linkOnly,
            firstBrokerLoginFlowAlias = idp.firstBrokerLoginFlowAlias,
            postBrokerLoginFlowAlias = idp.postBrokerLoginFlowAlias,
            updateProfileFirstLoginMode = idp.updateProfileFirstLoginMode
        )

        client.updateIdentityProvider(updateRequest, realm(), idp.alias)
    }

    override fun name() = "UnlinkIdentityProviderAction $identityProviderAlias from $organizationAlias"
}
