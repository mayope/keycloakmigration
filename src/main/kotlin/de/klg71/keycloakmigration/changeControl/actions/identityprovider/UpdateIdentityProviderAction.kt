package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.identityProviderExistsByAlias
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.IdentityProvider
import de.klg71.keycloakmigration.keycloakapi.model.UpdateIdentityProvider
import de.klg71.keycloakmigration.keycloakapi.model.fromExisting
import de.klg71.keycloakmigration.keycloakapi.model.patchIdentityProvider

@Suppress("LongParameterList")
class UpdateIdentityProviderAction(
    realm: String? = null,
    private val alias: String,
    private val providerId: String? = null,
    private val config: Map<String, String>? = null,
    private val displayName: String? = null,
    private val enabled: Boolean? = null,
    private val trustEmail: Boolean? = null,
    private val storeToken: Boolean? = null,
    private val linkOnly: Boolean? = null,
    private val firstBrokerLoginFlowAlias: String? = null,
    private val postBrokerLoginFlowAlias: String? = null,
    private val updateProfileFirstLoginMode: String? = null,
) : Action(realm) {

    private lateinit var identityProvider: IdentityProvider

    override fun execute() {
        if (client.identityProviderExistsByAlias(alias, realm())) {
            identityProvider = client.identityProvider(realm(), alias)
            client.updateIdentityProvider(updateIdentityProvider(identityProvider), realm(), alias).apply {
                if (!isSuccessful()) {
                    throw KeycloakApiException(this.body().asReader().readText())
                }
            }
        } else {
            throw MigrationException("Identity Provider with alias: $alias does not exist in realm: ${realm()}!")
        }
    }

    private fun updateIdentityProvider(idp: IdentityProvider): UpdateIdentityProvider =
        patchIdentityProvider(
            idp, providerId, alias, displayName, enabled, config, trustEmail, storeToken, linkOnly,
            firstBrokerLoginFlowAlias, postBrokerLoginFlowAlias, updateProfileFirstLoginMode
        )

    override fun undo() {
        client.updateIdentityProvider(fromExisting(identityProvider), realm(), alias)
    }

    override fun name() = "UpdateIdentityProvider $alias"

}
