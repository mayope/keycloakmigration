package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.identityProviderExistsByAlias
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.IdentityProvider
import de.klg71.keycloakmigration.keycloakapi.model.UpdateIdentityProvider
import de.klg71.keycloakmigration.keycloakapi.model.fromExisting
import de.klg71.keycloakmigration.keycloakapi.model.patchKeycloakIdentityProvider

@Suppress("LongParameterList")
class UpdateKeycloakIdentityProviderAction(
    realm: String? = null,
    private val alias: String,
    private val authorizationUrl: String? = null,
    private val tokenUrl: String? = null,
    private val clientId: String? = null,
    private val clientSecret: String? = null,
    private val clientAuthMethod: String? = null,
    private val logoutUrl: String? = null,
    private val userInfoUrl: String? = null,
    private val issuer: String? = null,
    private val displayName: String? = null,
    private val defaultScopes: List<String>? = null,
    private val validateSignature: String? = null,
    private val forwardParameters: List<String>? = null,
    private val loginHint: Boolean? = null,
    private val disableUserInfo: Boolean? = null,
    private val hideOnLoginPage: Boolean? = null,
    private val backchannelSupported: Boolean? = null,
    private val syncMode: String? = null,
    private val acceptsPromptsNoneForwardFromClient: String? = null,
    private val uiLocales: Boolean? = null,
    private val guiOrder: String? = null,
    private val useJwksUrl: Boolean? = null,
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
            client.updateIdentityProvider(updateKeycloakIdentityProvider(identityProvider), realm(), alias)
                .apply {
                    if (!isSuccessful()) {
                        throw KeycloakApiException(this.body().asReader().readText())
                    }
                }
        } else {
            throw MigrationException("Identity Provider with alias: $alias does not exist in realm: ${realm()}!")
        }
    }

    private fun updateKeycloakIdentityProvider(idp: IdentityProvider): UpdateIdentityProvider =
        patchKeycloakIdentityProvider(
            idp,
            authorizationUrl,
            tokenUrl,
            alias,
            clientId,
            clientSecret,
            clientAuthMethod,
            logoutUrl,
            userInfoUrl,
            issuer,
            defaultScopes,
            validateSignature,
            forwardParameters,
            loginHint,
            disableUserInfo,
            hideOnLoginPage,
            backchannelSupported,
            syncMode,
            acceptsPromptsNoneForwardFromClient,
            uiLocales,
            displayName,
            guiOrder,
            enabled,
            trustEmail,
            useJwksUrl,
            storeToken,
            linkOnly,
            firstBrokerLoginFlowAlias,
            postBrokerLoginFlowAlias,
            updateProfileFirstLoginMode,
        )

    override fun undo() {
        client.updateIdentityProvider(fromExisting(identityProvider), realm(), alias)
    }

    override fun name() = "UpdateKeycloakIdentityProvider $alias"

}
