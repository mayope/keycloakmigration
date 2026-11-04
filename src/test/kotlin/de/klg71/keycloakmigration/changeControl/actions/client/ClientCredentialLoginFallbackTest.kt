package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.TEST_BASE_URL
import de.klg71.keycloakmigration.keycloakapi.initKeycloakClient
import feign.FeignException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test

class ClientCredentialLoginFallbackTest : AbstractIntegrationTest() {

    private val loginRealm = "master"
    private val secret = "testSecret"
    private val createdClients = mutableListOf<String>()

    // Longer than the access token lifespan (60s) but shorter than the refresh token's, so every call refreshes
    private val alwaysRefreshOffsetMs = 61_000L

    @After
    fun deleteClients() {
        createdClients.forEach { DeleteClientAction(loginRealm, it).executeIt() }
    }

    private fun addClient(
        clientId: String,
        serviceAccountsEnabled: Boolean = true,
        attributes: Map<String, String> = mapOf()
    ) {
        AddSimpleClientAction(
            loginRealm, clientId, attributes = attributes, secret = secret,
            serviceAccountsEnabled = serviceAccountsEnabled, publicClient = false
        ).executeIt()
        createdClients += clientId
        if (serviceAccountsEnabled) {
            AssignRoleToClientAction(loginRealm, "admin", clientId).executeIt()
        }
    }

    private fun login(
        clientId: String,
        clientSecret: String = secret,
        fallback: Boolean = true,
        realm: String = loginRealm,
        tokenOffsetMs: Long = 1000
    ) = initKeycloakClient(
        TEST_BASE_URL, "admin", "admin", false, 8081, realm, clientId, clientSecret,
        loginWithClientCredentials = true, tokenOffsetMs = tokenOffsetMs, fallbackToPasswordLogin = fallback
    )

    @Test
    fun testFallsBackToPasswordWhenClientIsMissing() {
        assertThat(login("missing-client").realmNames()).isNotEmpty
    }

    @Test
    fun testFallsBackToPasswordWhenServiceAccountsAreDisabled() {
        addClient("no-service-account", serviceAccountsEnabled = false)

        assertThat(login("no-service-account").realmNames()).isNotEmpty
    }

    @Test
    fun testFallsBackToPasswordWhenSecretIsWrong() {
        addClient("wrong-secret")

        assertThat(login("wrong-secret", clientSecret = "not-the-secret").realmNames()).isNotEmpty
    }

    @Test
    fun testFailsWithoutFallbackWhenClientIsMissing() {
        assertThatThrownBy { login("missing-client", fallback = false) }
            .isInstanceOf(FeignException::class.java)
    }

    @Test
    fun testDoesNotFallBackOnOtherErrors() {
        assertThatThrownBy { login("missing-client", realm = "missing-realm") }
            .isInstanceOf(FeignException::class.java)
    }

    @Test
    fun testClientCredentialsWithoutRefreshToken() {
        addClient("no-refresh-token")

        // Every call needs a new token; without a refresh token each one logs in again
        val client = login("no-refresh-token", fallback = false, tokenOffsetMs = alwaysRefreshOffsetMs)

        assertThat(client.realmNames()).isNotEmpty
        assertThat(client.realmNames()).isNotEmpty
    }

    @Test
    fun testRefreshAfterFallbackUsesFallbackClient() {
        val client = login("missing-client", tokenOffsetMs = alwaysRefreshOffsetMs)

        assertThat(client.realmNames()).isNotEmpty
        assertThat(client.realmNames()).isNotEmpty
    }
}
