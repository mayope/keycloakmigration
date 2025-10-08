package de.klg71.keycloakmigration.changeControl.actions.clientscope

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.ProtocolMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.*

class AddClientScopeIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val scopeName = "integrationTest"

    @Test
    fun testAddScope() {
        AddClientScopeAction(
            testRealm, scopeName, protocolMappers = listOf(
                ProtocolMapper(
                    name = "username", protocol = "openid-connect", protocolMapper = "oidc-usermodel-property-mapper",
                    consentRequired = false, config = mapOf(
                        "userinfo.token.claim" to "true",
                        "user.attribute" to "username",
                        "id.token.claim" to "true",
                        "access.token.claim" to "true",
                        "claim.name" to "preferred_username",
                        "jsonType.label" to "String"
                    )
                )
            ),
            description = "testName",
            consentScreenText = "text for screen",
            displayOnConsentScreen = true,
            guiOrder = 3,
            includeInTokenScope = false,
            config = mapOf(
                "test.attribute" to "false",
                "another.test.attribute" to "12321"
            )
        ).executeIt()

        val scopes = client.clientScopes(testRealm)
        assertThat(scopes.any { it.name == scopeName }).isTrue()
        val theScope = scopes.find { it.name == scopeName }!!
        assertThat(theScope.protocolMappers).hasSize(1)
        assertThat(theScope.protocolMappers!!.first().name).isEqualTo("username")
        assertThat(theScope.protocolMappers!!.first().protocol).isEqualTo("openid-connect")
        assertThat(theScope.protocolMappers!!.first().protocolMapper).isEqualTo("oidc-usermodel-property-mapper")
        assertThat(theScope.protocolMappers!!.first().consentRequired).isEqualTo(false)
        assertThat(theScope.protocolMappers!!.first().config).containsExactlyInAnyOrderEntriesOf(
            mapOf("userinfo.token.claim" to "true",
                "user.attribute" to "username",
                "id.token.claim" to "true",
                "access.token.claim" to "true",
                "claim.name" to "preferred_username",
                "jsonType.label" to "String"
            )
        )
        assertThat(theScope.description).isEqualTo("testName")
        assertThat(theScope.attributes).contains(
                AbstractMap.SimpleEntry("test.attribute", "false"),
                AbstractMap.SimpleEntry("another.test.attribute", "12321"),
                AbstractMap.SimpleEntry("consent.screen.text", "text for screen"),
                AbstractMap.SimpleEntry("display.on.screen.consent", "true"),
                AbstractMap.SimpleEntry("gui.order", "3"),
                AbstractMap.SimpleEntry("include.in.token.scope", "false"),
        )
    }

    @Test
    fun testAddScopeAlreadyExisting() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        assertThatThrownBy {
            AddClientScopeAction(testRealm, scopeName).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("ClientScope with name: integrationTest already exists in realm: ${testRealm}!")
    }

    @Test
    fun testUndoAddScope() {
        AddClientScopeAction(testRealm, scopeName).run {
            executeIt()
            undoIt()
        }
        val scopes = client.clientScopes(testRealm)
        assertThat(scopes.any { it.name == scopeName }).isFalse()
    }
}
