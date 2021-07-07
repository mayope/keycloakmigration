package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import feign.FeignException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject

class DeleteRequiredActionActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteRequiredAction() {
        AddRequiredActionAction(
                testRealm, "UPDATE_PASSWORD", "update_password", "Update password",
                mapOf(
                        "foo" to "bar",
                        "foo1" to "bar1"
                ), false, true, 123
        ).executeIt()

        val requiredAction = client.requiredAction(testRealm, "update_password")
        assertThat(requiredAction).isNotNull()

        DeleteRequiredActionAction(testRealm, "update_password").executeIt()

        Assertions.assertThatThrownBy {
            client.requiredAction(testRealm, "update_password")
        }.isInstanceOf(FeignException::class.java).hasMessageContaining("404")
    }

    @Test
    fun testDeleteRequiredAction_notExisting() {
        DeleteRequiredActionAction(testRealm, "update_password").executeIt()

        Assertions.assertThatThrownBy {
            client.requiredAction(testRealm, "update_password")
        }.isInstanceOf(FeignException::class.java).hasMessageContaining("404")
    }

    @Test
    fun testDeleteRequiredAction_rollBack() {
        AddRequiredActionAction(
                testRealm, "UPDATE_PASSWORD", "update_password", "Update password",
                mapOf(
                        "foo" to "bar",
                        "foo1" to "bar1"
                ), false, true, 123
        ).executeIt()

        assertThat(client.requiredAction(testRealm, "update_password")).isNotNull()

        val action = DeleteRequiredActionAction(testRealm, "update_password")

        action.executeIt()
        Assertions.assertThatThrownBy {
            client.requiredAction(testRealm, "update_password")
        }.isInstanceOf(FeignException::class.java).hasMessageContaining("404")

        action.undoIt()
        assertThat(client.requiredAction(testRealm, "update_password")).isNotNull()
    }
}
