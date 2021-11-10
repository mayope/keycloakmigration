package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddKeycloakIdentityProviderIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddKeycloakIdentityProvider() {
        val authUrl = "https://testUrl"
        val tokenUrl = "https://tokenUrl"
        val clientId = "clientId"
        val clientSecret = "clientSecret"
        val issuer = "issuer"
        val scopes = listOf("scope1", "scope2")
        val config = mapOf(
            "authorizationUrl" to authUrl,
            "tokenUrl" to tokenUrl,
            "issuer" to issuer,
            "defaultScope" to "scope1,scope2"
        )
        AddKeycloakIdentityProviderAction(
            testRealm, "test", authUrl, tokenUrl, clientId, clientSecret, displayName = "displayName",
            defaultScopes = scopes, issuer = issuer
        ).executeIt()

        val identityProvider = client.identityProvider(
            testRealm, "test"
        )

        assertThat(identityProvider.alias).isEqualTo("test")
        assertThat(identityProvider.providerId).isEqualTo("keycloak-oidc")
        assertThat(identityProvider.displayName).isEqualTo("displayName")
        assertThat(identityProvider.enabled).isEqualTo(true)
        assertThat(identityProvider.trustEmail).isEqualTo(false)
        assertThat(identityProvider.storeToken).isEqualTo(false)
        assertThat(identityProvider.linkOnly).isEqualTo(false)
        assertThat(identityProvider.firstBrokerLoginFlowAlias).isEqualTo("first broker login")
        assertThat(identityProvider.postBrokerLoginFlowAlias).isEqualTo("")
        assertThat(identityProvider.updateProfileFirstLoginMode).isEqualTo("on")
        config.forEach {
            assertThat(identityProvider.config).containsEntry(it.key, it.value)
        }
    }

    @Test
    fun testAddKeycloakIdentityProvider_already_exist() {
        val authUrl = "https://testUrl"
        val tokenUrl = "https://tokenUrl"
        val clientId = "clientId"
        val clientSecret = "clientSecret"
        AddKeycloakIdentityProviderAction(
            testRealm, "test", authUrl, tokenUrl, clientId, clientSecret, displayName = "displayName"
        ).executeIt()

        assertThatThrownBy {
            AddKeycloakIdentityProviderAction(
                testRealm, "test", authUrl, tokenUrl, clientId, clientSecret, displayName = "displayName"
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Identity Provider with alias: test already exists in realm: test!")

    }
}
