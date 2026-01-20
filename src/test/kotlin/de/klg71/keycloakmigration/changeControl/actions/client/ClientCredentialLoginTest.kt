package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import org.junit.Test
import org.koin.core.component.inject
import org.koin.core.context.stopKoin

class ClientCredentialLoginTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testLoginClientCredentials() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val secret = "testSecret"
        UpdateClientAction(
            testRealm, "simpleClient", serviceAccountsEnabled = true, publicClient = false, secret = secret,
            clientAuthenticatorType = "client-secret", directAccessGrantEnabled = true,
            attributes = mapOf("client_credentials.use_refresh_token" to "true")
        ).executeIt()

        stopKoin()
        startKoinWithClientSecret("simpleClient", secret, testRealm)

        stopKoin()
        startKoinWithParameters(emptyMap())
    }


}
