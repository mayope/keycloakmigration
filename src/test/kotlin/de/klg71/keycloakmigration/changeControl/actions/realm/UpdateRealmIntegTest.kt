package de.klg71.keycloakmigration.changeControl.actions.realm

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.realmById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.core.component.inject

class UpdateRealmIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @After
    fun tearRealmDown() {
        try {
            DeleteRealmAction("testRealm").executeIt()
        } catch (ignored: Exception) {
        }

    }

    @Test
    fun testUpdateRealm() {
        AddRealmAction("testRealm", id = "testRealm").executeIt()
        UpdateRealmAction("testRealm", displayName = "myDisplayName").executeIt()

        assertThat(client.realmById("testRealm").displayName).isEqualTo("myDisplayName")
        DeleteRealmAction("testRealm").executeIt()
    }

    @Test
    fun testUpdateRealmNotExisting() {
        assertThatThrownBy {
            UpdateRealmAction("testRealm", displayName = "myDisplayName").executeIt()
        }
            .isInstanceOf(MigrationException::class.java)
            .hasMessage("Realm with id: testRealm does not exist!")
    }

    @Test
    fun testUpdateRealmMergeAttribute() {
        AddRealmAction("testRealm", id = "testRealm").executeIt()
        UpdateRealmAction("testRealm", attributes = mapOf("modify" to "modify", "constant" to "constant")).executeIt()
        UpdateRealmAction("testRealm", attributes = mapOf("modify" to "modified")).executeIt()

        assertThat(client.realmById("testRealm").attributes["modify"]).isEqualTo("modified")
        DeleteRealmAction("testRealm").executeIt()
    }

    @Test
    fun testUpdateRealmThemes() {
        AddRealmAction("testRealm", id = "testRealm").executeIt()

        assertThat(client.realmById("testRealm").accountTheme).isEqualTo(null)
        assertThat(client.realmById("testRealm").adminTheme).isEqualTo(null)
        assertThat(client.realmById("testRealm").emailTheme).isEqualTo(null)
        assertThat(client.realmById("testRealm").loginTheme).isEqualTo(null)

        UpdateRealmAction(
            "testRealm",
            accountTheme = "keycloak",
            adminTheme = "keycloak",
            emailTheme = "keycloak",
            loginTheme = "keycloak"
        ).executeIt()

        assertThat(client.realmById("testRealm").accountTheme).isEqualTo("keycloak")
        assertThat(client.realmById("testRealm").adminTheme).isEqualTo("keycloak")
        assertThat(client.realmById("testRealm").emailTheme).isEqualTo("keycloak")
        assertThat(client.realmById("testRealm").loginTheme).isEqualTo("keycloak")
        DeleteRealmAction("testRealm").executeIt()
    }

    @Test
    fun testUpdateRealmPasswordPolicy() {
        AddRealmAction("testRealm", id = "testRealm").executeIt()

        assertThat(client.realmById("testRealm").passwordPolicy).isEqualTo("")
        UpdateRealmAction(
            "testRealm",
            passwordPolicy = mapOf("maxLength" to "40", "notemail" to "dumdidum", "regularExpression" to "pat/d")
        ).executeIt()

        assertThat(client.realmById("testRealm").passwordPolicy).contains("maxLength(40)")
            .contains("notEmail(undefined)")
            .contains("regexPattern(pat/d)")

        UpdateRealmAction(
            "testRealm",
            passwordPolicy = mapOf()
        ).executeIt()

        assertThat(client.realmById("testRealm").passwordPolicy).contains("")

        DeleteRealmAction("testRealm").executeIt()
    }
}
