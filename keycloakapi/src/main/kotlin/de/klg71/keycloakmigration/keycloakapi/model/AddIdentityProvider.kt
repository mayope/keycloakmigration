package de.klg71.keycloakmigration.keycloakapi.model

data class AddIdentityProvider(
    val providerId: String,
    val alias: String,
    val displayName: String,
    val enabled: Boolean,
    val config: Map<String, String>,
    val trustEmail: Boolean,
    val storeToken: Boolean,
    val linkOnly: Boolean,
    val firstBrokerLoginFlowAlias: String,
    val postBrokerLoginFlowAlias: String,
    val updateProfileFirstLoginMode: String = "on"
)

fun addKeycloakIdentityProvider(
    authorizationUrl: String,
    tokenUrl: String,
    alias: String,
    clientId: String,
    clientSecret: String,
    clientAuthMethod: String = "client_secret_post",
    logoutUrl: String = "",
    userInfoUrl: String = "",
    issuer: String = "",
    defaultScopes: List<String> = emptyList(),
    validateSignature: String = "",
    forwardParameters: List<String> = emptyList(),
    loginHint: Boolean = false,
    disableUserInfo: Boolean = false,
    hideOnLoginPage: Boolean = false,
    backchannelSupported: Boolean = false,
    syncMode: String = "IMPORT",
    acceptsPromptNoneForwardFromClient: String = "",
    uiLocales: Boolean = true,
    displayName: String = "",
    guiOrder: String = "",
    enabled: Boolean = true,
    trustEmail: Boolean = false,
    useJwksUrl: Boolean = false,
    storeToken: Boolean = false,
    linkOnly: Boolean = false,
    firstBrokerLoginFlowAlias: String = "first broker login",
    postBrokerLoginFlowAlias: String = "",
    updateProfileFirstLoginMode: String = "on"
) = AddIdentityProvider(
    "keycloak-oidc", alias, displayName, enabled, mapOf(
        "acceptsPromptNoneForwardFromClient" to acceptsPromptNoneForwardFromClient,
        "authorizationUrl" to authorizationUrl,
        "backchannelSupported" to backchannelSupported.toString().toLowerCase(),
        "clientAuthMethod" to clientAuthMethod,
        "clientId" to clientId,
        "clientSecret" to clientSecret,
        "defaultScope" to defaultScopes.joinToString(","),
        "disableUserInfo" to disableUserInfo.toString().toLowerCase(),
        "forwardParameters" to forwardParameters.joinToString(","),
        "guiOrder" to guiOrder,
        "hideOnLoginPage" to hideOnLoginPage.toString().toLowerCase(),
        "issuer" to issuer,
        "loginHint" to loginHint.toString().toLowerCase(),
        "logoutUrl" to logoutUrl,
        "syncMode" to syncMode,
        "tokenUrl" to tokenUrl,
        "uiLocales" to uiLocales.toString().toLowerCase(),
        "useJwksUrl" to useJwksUrl.toString().toLowerCase(),
        "userInfoUrl" to userInfoUrl,
        "validateSignature" to validateSignature
    ), trustEmail, storeToken, linkOnly, firstBrokerLoginFlowAlias, postBrokerLoginFlowAlias,
    updateProfileFirstLoginMode
)

