package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.identityProviders
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class DeleteIdentityProviderIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteIdentityProvider() {
        val config = mapOf(
            "authorizationUrl" to "https://testUrl",
            "tokenUrl" to "https://testUrl",
            "issuer" to "issuer",
            "defaultScopes" to "scope1,scope2"
        )
        AddIdentityProviderAction(
            testRealm, "test", "keycloak-oidc", config, displayName = "displayName", true, true, true, true,
            "first broker login", ""
        ).executeIt()

        assertThat(client.identityProviders(testRealm).size).isOne()
        DeleteIdentityProviderAction(testRealm, "test").executeIt()
        assertThat(client.identityProviders(testRealm).size).isZero()
    }

    @Test
    fun testDeleteIdentityProvider_not_existing() {
        assertThat(client.identityProviders(testRealm).size).isZero()
        DeleteIdentityProviderAction(testRealm, "test").executeIt()
        assertThat(client.identityProviders(testRealm).size).isZero()
    }
}
