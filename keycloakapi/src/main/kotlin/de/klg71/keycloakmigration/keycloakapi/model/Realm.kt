package de.klg71.keycloakmigration.keycloakapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

data class IdentityProviderItem(val alias: String, val displayName: String? = null, val internalId: UUID)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Realm(
    val id: String,
    val realm: String,
    var displayName: String? = null,
    var displayNameHtml: String? = null,
    val notBefore: Int,
    val revokeRefreshToken: Boolean,
    val refreshTokenMaxReuse: Int,
    val accessTokenLifespan: Int,
    val accessTokenLifespanForImplicitFlow: Int,
    val ssoSessionIdleTimeout: Int,
    val ssoSessionMaxLifespan: Int,
    val ssoSessionIdleTimeoutRememberMe: Int,
    val ssoSessionMaxLifespanRememberMe: Int,
    val offlineSessionIdleTimeout: Int,
    val offlineSessionMaxLifespanEnabled: Boolean,
    val offlineSessionMaxLifespan: Int,
    val accessCodeLifespan: Int,
    val accessCodeLifespanUserAction: Int,
    val accessCodeLifespanLogin: Int,
    val actionTokenGeneratedByAdminLifespan: Int,
    val actionTokenGeneratedByUserLifespan: Int,
    val enabled: Boolean,
    val sslRequired: String,
    val registrationAllowed: Boolean,
    val registrationEmailAsUsername: Boolean,
    val rememberMe: Boolean,
    val verifyEmail: Boolean,
    val loginWithEmailAllowed: Boolean,
    val duplicateEmailsAllowed: Boolean,
    val resetPasswordAllowed: Boolean,
    val editUsernameAllowed: Boolean,
    val bruteForceProtected: Boolean,
    val permanentLockout: Boolean,
    val maxFailureWaitSeconds: Int,
    val minimumQuickLoginWaitSeconds: Int,
    val waitIncrementSeconds: Int,
    val quickLoginCheckMilliSeconds: Int,
    val maxDeltaTimeSeconds: Int,
    val failureFactor: Int,
    val requiredCredentials: List<String>,
    val otpPolicyType: String,
    val otpPolicyAlgorithm: String,
    val otpPolicyInitialCounter: Int,
    val otpPolicyDigits: Int,
    val otpPolicyLookAheadWindow: Int,
    val otpPolicyPeriod: Int,
    val otpSupportedApplications: List<String>,
    val webAuthnPolicyRpEntityName: String,
    val webAuthnPolicySignatureAlgorithms: List<String>,
    val webAuthnPolicyRpId: String,
    val webAuthnPolicyAttestationConveyancePreference: String,
    val webAuthnPolicyAuthenticatorAttachment: String,
    val webAuthnPolicyRequireResidentKey: String,
    val webAuthnPolicyUserVerificationRequirement: String,
    val webAuthnPolicyCreateTimeout: Int,
    val webAuthnPolicyAvoidSameAuthenticatorRegister: Boolean,
    val webAuthnPolicyAcceptableAaguids: List<String>,
    val browserSecurityHeaders: Map<String, String>,
    val smtpServer: Map<String, String>,
    val eventsEnabled: Boolean,
    val eventsListeners: List<String>,
    val enabledEventTypes: List<String>,
    val identityProviders: List<IdentityProviderItem> = emptyList(),
    val adminEventsEnabled: Boolean,
    val adminEventsDetailsEnabled: Boolean,
    val internationalizationEnabled: Boolean,
    val supportedLocales: List<String>,
    val browserFlow: String,
    val registrationFlow: String,
    val directGrantFlow: String,
    val resetCredentialsFlow: String,
    val clientAuthenticationFlow: String,
    val dockerAuthenticationFlow: String,
    val attributes: Map<String, String>,
    val userManagedAccessAllowed: Boolean,
    val accountTheme: String? = null,
    val adminTheme: String? = null,
    val emailTheme: String? = null,
    val loginTheme: String? = null,
    val requiredActions: List<RequiredActionProviderItem>? = null
)


class RealmUpdateBuilder(private val existingRealm: Realm) {
    var id: String = existingRealm.id
    var realm: String = existingRealm.realm
    var displayName: String? = existingRealm.displayName
    var displayNameHtml: String? = existingRealm.displayNameHtml
    var notBefore: Int = existingRealm.notBefore
    var revokeRefreshToken: Boolean = existingRealm.revokeRefreshToken
    var refreshTokenMaxReuse: Int = existingRealm.refreshTokenMaxReuse
    var accessTokenLifespan: Int = existingRealm.accessTokenLifespan
    var accessTokenLifespanForImplicitFlow: Int = existingRealm.accessTokenLifespanForImplicitFlow
    var ssoSessionIdleTimeout: Int = existingRealm.ssoSessionIdleTimeout
    var ssoSessionMaxLifespan: Int = existingRealm.ssoSessionMaxLifespan
    var ssoSessionIdleTimeoutRememberMe: Int = existingRealm.ssoSessionIdleTimeoutRememberMe
    var ssoSessionMaxLifespanRememberMe: Int = existingRealm.ssoSessionMaxLifespanRememberMe
    var offlineSessionIdleTimeout: Int = existingRealm.offlineSessionIdleTimeout
    var offlineSessionMaxLifespanEnabled: Boolean = existingRealm.offlineSessionMaxLifespanEnabled
    var offlineSessionMaxLifespan: Int = existingRealm.offlineSessionMaxLifespan
    var accessCodeLifespan: Int = existingRealm.accessCodeLifespan
    var accessCodeLifespanUserAction: Int = existingRealm.accessCodeLifespanUserAction
    var accessCodeLifespanLogin: Int = existingRealm.accessCodeLifespanLogin
    var actionTokenGeneratedByAdminLifespan: Int = existingRealm.actionTokenGeneratedByAdminLifespan
    var actionTokenGeneratedByUserLifespan: Int = existingRealm.actionTokenGeneratedByUserLifespan
    var enabled: Boolean = existingRealm.enabled
    var sslRequired: String = existingRealm.sslRequired
    var registrationAllowed: Boolean = existingRealm.registrationAllowed
    var registrationEmailAsUsername: Boolean = existingRealm.registrationEmailAsUsername
    var rememberMe: Boolean = existingRealm.rememberMe
    var verifyEmail: Boolean = existingRealm.verifyEmail
    var loginWithEmailAllowed: Boolean = existingRealm.loginWithEmailAllowed
    var duplicateEmailsAllowed: Boolean = existingRealm.duplicateEmailsAllowed
    var resetPasswordAllowed: Boolean = existingRealm.resetPasswordAllowed
    var editUsernameAllowed: Boolean = existingRealm.editUsernameAllowed
    var bruteForceProtected: Boolean = existingRealm.bruteForceProtected
    var permanentLockout: Boolean = existingRealm.permanentLockout
    var maxFailureWaitSeconds: Int = existingRealm.maxFailureWaitSeconds
    var minimumQuickLoginWaitSeconds: Int = existingRealm.minimumQuickLoginWaitSeconds
    var waitIncrementSeconds: Int = existingRealm.waitIncrementSeconds
    var quickLoginCheckMilliSeconds: Int = existingRealm.quickLoginCheckMilliSeconds
    var maxDeltaTimeSeconds: Int = existingRealm.maxDeltaTimeSeconds
    var failureFactor: Int = existingRealm.failureFactor
    var requiredCredentials: List<String> = existingRealm.requiredCredentials
    var otpPolicyType: String = existingRealm.otpPolicyType
    var otpPolicyAlgorithm: String = existingRealm.otpPolicyAlgorithm
    var otpPolicyInitialCounter: Int = existingRealm.otpPolicyInitialCounter
    var otpPolicyDigits: Int = existingRealm.otpPolicyDigits
    var otpPolicyLookAheadWindow: Int = existingRealm.otpPolicyLookAheadWindow
    var otpPolicyPeriod: Int = existingRealm.otpPolicyPeriod
    var otpSupportedApplications: List<String> = existingRealm.otpSupportedApplications
    var webAuthnPolicyRpEntityName: String = existingRealm.webAuthnPolicyRpEntityName
    var webAuthnPolicySignatureAlgorithms: List<String> = existingRealm.webAuthnPolicySignatureAlgorithms
    var webAuthnPolicyRpId: String = existingRealm.webAuthnPolicyRpId
    var webAuthnPolicyAttestationConveyancePreference: String =
        existingRealm.webAuthnPolicyAttestationConveyancePreference
    var webAuthnPolicyAuthenticatorAttachment: String = existingRealm.webAuthnPolicyAuthenticatorAttachment
    var webAuthnPolicyRequireResidentKey: String = existingRealm.webAuthnPolicyRequireResidentKey
    var webAuthnPolicyUserVerificationRequirement: String = existingRealm.webAuthnPolicyUserVerificationRequirement
    var webAuthnPolicyCreateTimeout: Int = existingRealm.webAuthnPolicyCreateTimeout
    var webAuthnPolicyAvoidSameAuthenticatorRegister: Boolean =
        existingRealm.webAuthnPolicyAvoidSameAuthenticatorRegister
    var webAuthnPolicyAcceptableAaguids: List<String> = existingRealm.webAuthnPolicyAcceptableAaguids
    var browserSecurityHeaders: Map<String, String> = existingRealm.browserSecurityHeaders
    var smtpServer: Map<String, String> = existingRealm.smtpServer
    var eventsEnabled: Boolean = existingRealm.eventsEnabled
    var eventsListeners: List<String> = existingRealm.eventsListeners
    var enabledEventTypes: List<String> = existingRealm.enabledEventTypes
    var identityProviders: List<IdentityProviderItem> = existingRealm.identityProviders
    var adminEventsEnabled: Boolean = existingRealm.adminEventsEnabled
    var adminEventsDetailsEnabled: Boolean = existingRealm.adminEventsDetailsEnabled
    var internationalizationEnabled: Boolean = existingRealm.internationalizationEnabled
    var supportedLocales: List<String> = existingRealm.supportedLocales
    var browserFlow: String = existingRealm.browserFlow
    var registrationFlow: String = existingRealm.registrationFlow
    var directGrantFlow: String = existingRealm.directGrantFlow
    var resetCredentialsFlow: String = existingRealm.resetCredentialsFlow
    var clientAuthenticationFlow: String = existingRealm.clientAuthenticationFlow
    var dockerAuthenticationFlow: String = existingRealm.dockerAuthenticationFlow
    var attributes: Map<String, String> = existingRealm.attributes
    var userManagedAccessAllowed: Boolean = existingRealm.userManagedAccessAllowed
    var accountTheme: String? = existingRealm.accountTheme
    var adminTheme: String? = existingRealm.adminTheme
    var emailTheme: String? = existingRealm.emailTheme
    var loginTheme: String? = existingRealm.loginTheme
    var requiredActions: List<RequiredActionProviderItem>? = existingRealm.requiredActions

    fun build() = Realm(

        id,
        realm,
        displayName,
        displayNameHtml,
        notBefore,
        revokeRefreshToken,
        refreshTokenMaxReuse,
        accessTokenLifespan,
        accessTokenLifespanForImplicitFlow,
        ssoSessionIdleTimeout,
        ssoSessionMaxLifespan,
        ssoSessionIdleTimeoutRememberMe,
        ssoSessionMaxLifespanRememberMe,
        offlineSessionIdleTimeout,
        offlineSessionMaxLifespanEnabled,
        offlineSessionMaxLifespan,
        accessCodeLifespan,
        accessCodeLifespanUserAction,
        accessCodeLifespanLogin,
        actionTokenGeneratedByAdminLifespan,
        actionTokenGeneratedByUserLifespan,
        enabled,
        sslRequired,
        registrationAllowed,
        registrationEmailAsUsername,
        rememberMe,
        verifyEmail,
        loginWithEmailAllowed,
        duplicateEmailsAllowed,
        resetPasswordAllowed,
        editUsernameAllowed,
        bruteForceProtected,
        permanentLockout,
        maxFailureWaitSeconds,
        minimumQuickLoginWaitSeconds,
        waitIncrementSeconds,
        quickLoginCheckMilliSeconds,
        maxDeltaTimeSeconds,
        failureFactor,
        requiredCredentials,
        otpPolicyType,
        otpPolicyAlgorithm,
        otpPolicyInitialCounter,
        otpPolicyDigits,
        otpPolicyLookAheadWindow,
        otpPolicyPeriod,
        otpSupportedApplications,
        webAuthnPolicyRpEntityName,
        webAuthnPolicySignatureAlgorithms,
        webAuthnPolicyRpId,
        webAuthnPolicyAttestationConveyancePreference,
        webAuthnPolicyAuthenticatorAttachment,
        webAuthnPolicyRequireResidentKey,
        webAuthnPolicyUserVerificationRequirement,
        webAuthnPolicyCreateTimeout,
        webAuthnPolicyAvoidSameAuthenticatorRegister,
        webAuthnPolicyAcceptableAaguids,
        browserSecurityHeaders,
        smtpServer,
        eventsEnabled,
        eventsListeners,
        enabledEventTypes,
        identityProviders,
        adminEventsEnabled,
        adminEventsDetailsEnabled,
        internationalizationEnabled,
        supportedLocales,
        browserFlow,
        registrationFlow,
        directGrantFlow,
        resetCredentialsFlow,
        clientAuthenticationFlow,
        dockerAuthenticationFlow,
        attributes,
        userManagedAccessAllowed,
        accountTheme,
        adminTheme,
        emailTheme,
        loginTheme,
        requiredActions
    )
}
