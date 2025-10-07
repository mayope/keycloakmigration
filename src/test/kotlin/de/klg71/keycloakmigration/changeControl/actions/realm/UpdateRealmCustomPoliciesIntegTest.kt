package de.klg71.keycloakmigration.changeControl.actions.realm

import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.PasswordPolicy
import de.klg71.keycloakmigration.keycloakapi.model.Realm
import de.klg71.keycloakmigration.keycloakapi.model.ServerInfo
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.logger.SLF4JLogger
import org.koin.test.KoinTest

class UpdateRealmCustomPoliciesIntegTest: KoinTest {

    private val client = mockk<KeycloakClient>(relaxed = true)

    @Before
    fun setup() {
        clearAllMocks()
        startKoin {
            logger(SLF4JLogger())
            modules(module {
                single { client }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testUpdateRealmCustomPasswordPolicy() {
        every {
            client.realms()
        }.returns(listOf(mockRealm()))

        val mockServerInfo = mockk<ServerInfo>()
        every {
            mockServerInfo.passwordPolicies
        } returns listOf(
            PasswordPolicy(
                id = "customPolicy",
                displayName = "customPolicy",
                multipleSupported = false
            )
        )

        every {
            client.serverInfo()
        }.returns(mockServerInfo)

        assertThatNoException().isThrownBy {
            UpdateRealmAction(
                "testRealm",
                passwordPolicy = mapOf("customPolicy" to "test")
            ).executeIt()
        }
    }

    @Suppress("LongMethod")
    private fun mockRealm(): Realm = Realm(
        id = "testRealm",
        realm = "testRealm",
        notBefore = 0,
        revokeRefreshToken = false,
        refreshTokenMaxReuse = 0,
        accessTokenLifespan = 0,
        accessTokenLifespanForImplicitFlow = 0,
        ssoSessionIdleTimeout = 0,
        ssoSessionMaxLifespan = 0,
        ssoSessionIdleTimeoutRememberMe = 0,
        ssoSessionMaxLifespanRememberMe = 0,
        offlineSessionIdleTimeout = 0,
        offlineSessionMaxLifespanEnabled = false,
        offlineSessionMaxLifespan = 0,
        accessCodeLifespan = 0,
        accessCodeLifespanUserAction = 0,
        accessCodeLifespanLogin = 0,
        actionTokenGeneratedByAdminLifespan = 0,
        actionTokenGeneratedByUserLifespan = 0,
        enabled = false,
        sslRequired = "test",
        registrationAllowed = false,
        registrationEmailAsUsername = false,
        rememberMe = false,
        verifyEmail = false,
        loginWithEmailAllowed = false,
        duplicateEmailsAllowed = false,
        resetPasswordAllowed = false,
        editUsernameAllowed = false,
        bruteForceProtected = false,
        permanentLockout = false,
        maxFailureWaitSeconds = 0,
        minimumQuickLoginWaitSeconds = 0,
        waitIncrementSeconds = 0,
        quickLoginCheckMilliSeconds = 0,
        maxDeltaTimeSeconds = 0,
        failureFactor = 0,
        requiredCredentials = listOf(),
        otpPolicyType = "test",
        otpPolicyAlgorithm = "test",
        otpPolicyInitialCounter = 0,
        otpPolicyDigits = 0,
        otpPolicyLookAheadWindow = 0,
        otpPolicyPeriod = 0,
        otpSupportedApplications = listOf(),
        webAuthnPolicyRpEntityName = "test",
        webAuthnPolicySignatureAlgorithms = listOf(),
        webAuthnPolicyRpId = "test",
        webAuthnPolicyAttestationConveyancePreference = "test",
        webAuthnPolicyAuthenticatorAttachment = "test",
        webAuthnPolicyRequireResidentKey = "test",
        webAuthnPolicyUserVerificationRequirement = "test",
        webAuthnPolicyCreateTimeout = 0,
        webAuthnPolicyAvoidSameAuthenticatorRegister = false,
        webAuthnPolicyAcceptableAaguids = listOf(),
        browserSecurityHeaders = mapOf(),
        smtpServer = mapOf(),
        eventsEnabled = false,
        eventsListeners = listOf(),
        eventsExpiration = 0,
        enabledEventTypes = listOf(),
        adminEventsEnabled = false,
        adminEventsDetailsEnabled = false,
        internationalizationEnabled = false,
        supportedLocales = listOf(),
        browserFlow = "test",
        registrationFlow = "test",
        directGrantFlow = "test",
        resetCredentialsFlow = "test",
        clientAuthenticationFlow = "test",
        dockerAuthenticationFlow = "test",
        attributes = mapOf(),
        userManagedAccessAllowed = false
    )
}
