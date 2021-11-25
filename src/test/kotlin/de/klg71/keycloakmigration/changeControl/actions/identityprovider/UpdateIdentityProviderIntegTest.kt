package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class UpdateIdentityProviderIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testUpdateIdentityProvider() {
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
        val newConfig = mapOf(
            "issuer" to "issuer new",
        )
        UpdateIdentityProviderAction(
            testRealm, "test", "keycloak-oidc", newConfig, displayName = "displayName new"
        ).executeIt()

        val identityProvider = client.identityProvider("test", "test")

        assertThat(identityProvider.alias).isEqualTo("test")
        assertThat(identityProvider.providerId).isEqualTo("keycloak-oidc")
        assertThat(identityProvider.displayName).isEqualTo("displayName new")
        assertThat(identityProvider.enabled).isEqualTo(true)
        assertThat(identityProvider.trustEmail).isEqualTo(true)
        assertThat(identityProvider.storeToken).isEqualTo(true)
        assertThat(identityProvider.linkOnly).isEqualTo(true)
        assertThat(identityProvider.firstBrokerLoginFlowAlias).isEqualTo("first broker login")
        assertThat(identityProvider.postBrokerLoginFlowAlias).isEqualTo("")
        assertThat(identityProvider.updateProfileFirstLoginMode).isEqualTo("on")
        newConfig.forEach {
            assertThat(identityProvider.config).containsEntry(it.key, it.value)
        }
    }

    @Test
    fun testUpdateIdentityProvider_undo() {
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
        val newConfig = mapOf(
            "authorizationUrl" to "https://testUrl",
            "tokenUrl" to "https://testUrl",
            "issuer" to "issuer new",
            "defaultScopes" to "scope1,scope2"
        )
        val updateAction = UpdateIdentityProviderAction(
            testRealm, "test", "keycloak-oidc", newConfig, displayName = "displayName new", true, true, true, true,
            "first broker login", "", ""
        )
        updateAction.executeIt()
        updateAction.undoIt()

        val identityProvider = client.identityProvider("test", "test")

        assertThat(identityProvider.alias).isEqualTo("test")
        assertThat(identityProvider.providerId).isEqualTo("keycloak-oidc")
        assertThat(identityProvider.displayName).isEqualTo("displayName")
        assertThat(identityProvider.enabled).isEqualTo(true)
        assertThat(identityProvider.trustEmail).isEqualTo(true)
        assertThat(identityProvider.storeToken).isEqualTo(true)
        assertThat(identityProvider.linkOnly).isEqualTo(true)
        assertThat(identityProvider.firstBrokerLoginFlowAlias).isEqualTo("first broker login")
        assertThat(identityProvider.postBrokerLoginFlowAlias).isEqualTo("")
        assertThat(identityProvider.updateProfileFirstLoginMode).isEqualTo("on")
        config.forEach {
            assertThat(identityProvider.config).containsEntry(it.key, it.value)
        }
    }

    @Test
    fun testUpdateIdentityProvider_not_existing() {
        val config = mapOf(
            "authorizationUrl" to "https://testUrl",
            "tokenUrl" to "https://testUrl",
            "issuer" to "issuer",
            "defaultScopes" to "scope1,scope2"
        )

        assertThatThrownBy {
            UpdateIdentityProviderAction(
                testRealm, "test", "keycloak-oidc", config, displayName = "displayName", true, true, true, true,
                "first broker login", "", ""
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Identity Provider with alias: test does not exist in realm: test!")

    }
}
