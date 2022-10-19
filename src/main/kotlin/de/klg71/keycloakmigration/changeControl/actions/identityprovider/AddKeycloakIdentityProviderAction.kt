package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.identityProviderExistsByAlias
import de.klg71.keycloakmigration.keycloakapi.identityProviderItems
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProvider
import de.klg71.keycloakmigration.keycloakapi.model.addKeycloakIdentityProvider

@Suppress("LongParameterList")
class AddKeycloakIdentityProviderAction(
    realm: String? = null,
    private val alias: String,
    private val authorizationUrl: String,
    private val tokenUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val clientAuthMethod: String = "client_secret_post",
    private val logoutUrl: String = "",
    private val userInfoUrl: String = "",
    private val issuer: String = "",
    private val displayName: String = "",
    private val defaultScopes: List<String> = emptyList(),
    private val validateSignature: String = "",
    private val forwardParameters: List<String> = emptyList(),
    private val loginHint: Boolean = false,
    private val disableUserInfo: Boolean = false,
    private val hideOnLoginPage: Boolean = false,
    private val backchannelSupported: Boolean = false,
    private val syncMode: String = "IMPORT",
    private val acceptsPromptsNoneForwardFromClient: String = "",
    private val uiLocales: Boolean = true,
    private val guiOrder: String = "",
    private val useJwksUrl: Boolean = false,
    private val enabled: Boolean = true,
    private val trustEmail: Boolean = false,
    private val storeToken: Boolean = false,
    private val linkOnly: Boolean = false,
    private val firstBrokerLoginFlowAlias: String = "first broker login",
    private val postBrokerLoginFlowAlias: String = "",
    private val updateProfileFirstLoginMode: String = "on",
) : Action(realm) {


    override fun execute() {
        if (client.identityProviderExistsByAlias(alias, realm())) {
            throw MigrationException("Identity Provider with alias: $alias already exists in realm: ${realm()}!")
        }
        client.addIdentityProvider(addIdentityProvider(), realm())
    }

    private fun addIdentityProvider(): AddIdentityProvider =
        addKeycloakIdentityProvider(
            authorizationUrl, tokenUrl,
            alias, clientId, clientSecret, clientAuthMethod, logoutUrl, userInfoUrl, issuer, defaultScopes,
            validateSignature, forwardParameters, loginHint, disableUserInfo, hideOnLoginPage, backchannelSupported,
            syncMode, acceptsPromptsNoneForwardFromClient, uiLocales, displayName, guiOrder, enabled, trustEmail,
            useJwksUrl, storeToken, linkOnly,
            firstBrokerLoginFlowAlias, postBrokerLoginFlowAlias, updateProfileFirstLoginMode
        )

    override fun undo() {
        client.identityProviderItems(realm()).find {
            it.alias == alias
        }?.let {
            client.deleteIdentityProvider(realm(), it.alias)
        }
    }


    override fun name() = "AddKeycloakIdentityProvider $alias"

}
