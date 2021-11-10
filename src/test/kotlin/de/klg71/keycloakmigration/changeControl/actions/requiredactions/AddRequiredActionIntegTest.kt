package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import feign.FeignException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.AbstractMap

class AddRequiredActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddRequiredAction() {
        AddRequiredActionAction(
            testRealm, "UPDATE_PASSWORD", "update_password", "Update password",
            mapOf(
                "foo" to "bar",
                "foo1" to "bar1"
            ), false, true, 123
        ).executeIt()

        val requiredAction = client.requiredAction(testRealm, "update_password")
        assertThat(requiredAction).isNotNull()
        assertThat(requiredAction.providerId).isEqualTo("UPDATE_PASSWORD")
        assertThat(requiredAction.alias).isEqualTo("update_password")
        assertThat(requiredAction.name).isEqualTo("Update password")
        assertThat(requiredAction.config).containsExactly(
            AbstractMap.SimpleEntry("foo", "bar"),
            AbstractMap.SimpleEntry("foo1", "bar1")
        )
        assertThat(requiredAction.defaultAction).isEqualTo(false)
        assertThat(requiredAction.enabled).isEqualTo(true)
        assertThat(requiredAction.priority).isEqualTo(123)
    }

    @Test
    fun testRequiredAction_Existing() {
        AddRequiredActionAction(
            testRealm, "UPDATE_PASSWORD", "update_password", "Update password",
            mapOf(
                "foo" to "bar",
                "foo1" to "bar1"
            ), false, true, 123
        ).executeIt()

        assertThatThrownBy {
            AddRequiredActionAction(
                testRealm, "UPDATE_PASSWORD", "update_password", "Update password",
                mapOf(
                    "foo" to "bar",
                    "foo1" to "bar1"
                ), false, true, 123
            ).executeIt()
        }.isInstanceOf(KeycloakApiException::class.java)
            .hasMessage("Import RequiredAction failed, RequiredAction: update_password already exists")
    }

    @Test
    fun testAddRequiredAction_Rollback() {
        val action = AddRequiredActionAction(
            testRealm, "UPDATE_PASSWORD", "update_password", "Update password",
            mapOf(
                "foo" to "bar",
                "foo1" to "bar1"
            ), false, true, 123
        )

        action.executeIt()
        assertThat(client.requiredAction(testRealm, "update_password")).isNotNull()

        action.undoIt()
        assertThatThrownBy {
            client.requiredAction(testRealm, "update_password")
        }.isInstanceOf(FeignException::class.java).hasMessageContaining("404")
    }
}
