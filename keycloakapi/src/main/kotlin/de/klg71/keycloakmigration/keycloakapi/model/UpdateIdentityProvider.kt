package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class UpdateIdentityProvider(
    val internalId: UUID,
    val providerId: String,
    val alias: String,
    val displayName: String? = null,
    val enabled: Boolean,
    val config: Map<String, String>,
    val trustEmail: Boolean,
    val storeToken: Boolean,
    val linkOnly: Boolean,
    val firstBrokerLoginFlowAlias: String,
    val postBrokerLoginFlowAlias: String,
    val updateProfileFirstLoginMode: String = "on"
)

fun patchIdentityProvider(
    existingIdp: IdentityProvider,
    providerId: String?,
    alias: String?,
    displayName: String?,
    enabled: Boolean?,
    config: Map<String, String>?,
    trustEmail: Boolean?,
    storeToken: Boolean?,
    linkOnly: Boolean?,
    firstBrokerLoginFlowAlias: String?,
    postBrokerLoginFlowAlias: String?,
    updateProfileFirstLoginMode: String?,
) = UpdateIdentityProvider(
    existingIdp.internalId,
    providerId ?: existingIdp.providerId,
    alias ?: existingIdp.alias,
    displayName ?: existingIdp.displayName,
    enabled ?: existingIdp.enabled,
    patchIdpConfig(existingIdp, config),
    trustEmail ?: existingIdp.trustEmail,
    storeToken ?: existingIdp.storeToken,
    linkOnly ?: existingIdp.linkOnly,
    firstBrokerLoginFlowAlias ?: existingIdp.firstBrokerLoginFlowAlias,
    postBrokerLoginFlowAlias ?: existingIdp.postBrokerLoginFlowAlias,
    updateProfileFirstLoginMode ?: existingIdp.updateProfileFirstLoginMode,
)

fun patchIdpConfig(existingIdp: IdentityProvider, newConfig: Map<String, String>?): Map<String, String> {
    val config = mutableMapOf<String, String>()
    config.putAll(existingIdp.config)
    newConfig?.let { config.putAll(it) }
    return config.toMap()
}

fun fromExisting(
    idp: IdentityProvider
) = UpdateIdentityProvider(
    idp.internalId,
    idp.providerId,
    idp.alias,
    idp.displayName,
    idp.enabled,
    idp.config,
    idp.trustEmail,
    idp.storeToken,
    idp.linkOnly,
    idp.firstBrokerLoginFlowAlias,
    idp.postBrokerLoginFlowAlias,
    idp.updateProfileFirstLoginMode
)

fun patchKeycloakIdentityProvider(
    existingIdp: IdentityProvider,
    authorizationUrl: String? = null,
    tokenUrl: String? = null,
    alias: String? = null,
    clientId: String? = null,
    clientSecret: String? = null,
    clientAuthMethod: String? = null,
    logoutUrl: String? = null,
    userInfoUrl: String? = null,
    issuer: String? = null,
    defaultScopes: List<String>? = null,
    validateSignature: String? = null,
    forwardParameters: List<String>? = null,
    loginHint: Boolean? = null,
    disableUserInfo: Boolean? = null,
    hideOnLoginPage: Boolean? = null,
    backchannelSupported: Boolean? = null,
    syncMode: String? = null,
    acceptsPromptNoneForwardFromClient: String? = null,
    uiLocales: Boolean? = null,
    displayName: String? = null,
    guiOrder: String? = null,
    enabled: Boolean? = null,
    trustEmail: Boolean? = null,
    useJwksUrl: Boolean? = null,
    storeToken: Boolean? = null,
    linkOnly: Boolean? = null,
    firstBrokerLoginFlowAlias: String? = null,
    postBrokerLoginFlowAlias: String? = null,
    updateProfileFirstLoginMode: String? = null,
) = UpdateIdentityProvider(
    existingIdp.internalId,
    "keycloak-oidc",
    alias ?: existingIdp.alias,
    displayName ?: existingIdp.displayName,
    enabled ?: existingIdp.enabled,
    patchKeycloakIdpConfig(
        existingIdp,
        acceptsPromptNoneForwardFromClient,
        authorizationUrl,
        backchannelSupported,
        clientAuthMethod,
        clientId,
        clientSecret,
        defaultScopes,
        disableUserInfo,
        forwardParameters,
        guiOrder,
        hideOnLoginPage,
        issuer,
        loginHint,
        logoutUrl,
        syncMode,
        tokenUrl,
        uiLocales,
        useJwksUrl,
        userInfoUrl,
        validateSignature
    ),
    trustEmail ?: existingIdp.trustEmail,
    storeToken ?: existingIdp.storeToken,
    linkOnly ?: existingIdp.linkOnly,
    firstBrokerLoginFlowAlias ?: existingIdp.firstBrokerLoginFlowAlias,
    postBrokerLoginFlowAlias ?: existingIdp.postBrokerLoginFlowAlias,
    updateProfileFirstLoginMode ?: existingIdp.updateProfileFirstLoginMode,
)

private fun patchKeycloakIdpConfig(
    existingIdp: IdentityProvider,
    acceptsPromptNoneForwardFromClient: String?,
    authorizationUrl: String?,
    backchannelSupported: Boolean?,
    clientAuthMethod: String?,
    clientId: String?,
    clientSecret: String?,
    defaultScopes: List<String>?,
    disableUserInfo: Boolean?,
    forwardParameters: List<String>?,
    guiOrder: String?,
    hideOnLoginPage: Boolean?,
    issuer: String?,
    loginHint: Boolean?,
    logoutUrl: String?,
    syncMode: String?,
    tokenUrl: String?,
    uiLocales: Boolean?,
    useJwksUrl: Boolean?,
    userInfoUrl: String?,
    validateSignature: String?
): Map<String, String> {
    val config = mutableMapOf<String, String>()
    config.putAll(existingIdp.config)

    acceptsPromptNoneForwardFromClient?.let { config.put("acceptsPromptNoneForwardFromClient", it) }
    authorizationUrl?.let { config.put("authorizationUrl", it) }
    backchannelSupported?.let { config.put("backchannelSupported", it.toString().toLowerCase()) }
    clientAuthMethod?.let { config.put("clientAuthMethod", it) }
    clientId?.let { config.put("clientId", it) }
    clientSecret?.let { config.put("clientSecret", it) }
    defaultScopes?.let { config.put("defaultScope", it.joinToString(",")) }
    disableUserInfo?.let { config.put("disableUserInfo", it.toString().toLowerCase()) }
    forwardParameters?.let { config.put("forwardParameters", it.joinToString(",")) }
    guiOrder?.let { config.put("guiOrder", it) }
    hideOnLoginPage?.let { config.put("hideOnLoginPage", it.toString().toLowerCase()) }
    issuer?.let { config.put("issuer", it) }
    loginHint?.let { config.put("loginHint", it.toString().toLowerCase()) }
    logoutUrl?.let { config.put("logoutUrl", it) }
    syncMode?.let { config.put("syncMode", it) }
    tokenUrl?.let { config.put("tokenUrl", it) }
    uiLocales?.let { config.put("uiLocales", it.toString().toLowerCase()) }
    useJwksUrl?.let { config.put("useJwksUrl", it.toString().toLowerCase()) }
    userInfoUrl?.let { config.put("userInfoUrl", it) }
    validateSignature?.let { config.put("validateSignature", it) }

    return config.toMap()
}

