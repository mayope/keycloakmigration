package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.identityProviderExistsByAlias
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProvider

@Suppress("LongParameterList")
class AddIdentityProviderAction(
    realm: String? = null,
    private val alias: String,
    private val providerId: String,
    private val config: Map<String, String>,
    private val displayName: String = "",
    private val enabled: Boolean = true,
    private val trustEmail: Boolean = false,
    private val storeToken: Boolean = false,
    private val linkOnly: Boolean = false,
    private val firstBrokerLoginFlowAlias: String = "first broker login",
    private val postBrokerLoginFlowAlias: String = "",
) : Action(realm) {


    override fun execute() {
        if (client.identityProviderExistsByAlias(alias, realm())) {
            throw MigrationException("Identity Provider with alias: $alias already exists in realm: ${realm()}!")
        }
        client.addIdentityProvider(addIdentityProvider(), realm()).apply {
            if (!isSuccessful()) {
                throw KeycloakApiException(this.body().asReader().readText(),status())
            }
        }
    }

    private fun addIdentityProvider(): AddIdentityProvider =
        AddIdentityProvider(
            providerId, alias, displayName, enabled, config, trustEmail, storeToken, linkOnly,
            firstBrokerLoginFlowAlias, postBrokerLoginFlowAlias
        )

    override fun undo() {
        client.identityProviders(realm()).find {
            it.alias == alias
        }?.let {
            client.deleteIdentityProvider(realm(), it.alias)
        }
    }


    override fun name() = "AddIdentityProvider $alias"

}
