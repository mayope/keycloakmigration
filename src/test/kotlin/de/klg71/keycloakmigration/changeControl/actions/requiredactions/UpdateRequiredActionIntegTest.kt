package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import feign.FeignException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject
import java.util.AbstractMap

class UpdateRequiredActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testUpdateRequiredAction() {
        val requiredAction = client.requiredAction(testRealm, "UPDATE_PASSWORD")
        UpdateRequiredActionAction(
            testRealm, requiredAction.alias, null, "update_password2", "Update password 2",
            mapOf(
                "foo" to "bar",
                "foo3" to "bar3"
            ), true, null, 321
        ).executeIt()

        Assertions.assertThatThrownBy {
            client.requiredAction(testRealm, "update_password")
        }.isInstanceOf(FeignException::class.java).hasMessageContaining("404")

        val updatedRequiredAction = client.requiredAction(testRealm, "update_password2")
        assertThat(updatedRequiredAction).isNotNull()
        assertThat(updatedRequiredAction.alias).isEqualTo("update_password2")
        assertThat(updatedRequiredAction.providerId).isEqualTo(requiredAction.providerId)
        assertThat(updatedRequiredAction.name).isEqualTo("Update password 2")
        assertThat(updatedRequiredAction.config).containsExactly(
            AbstractMap.SimpleEntry("foo3", "bar3"),
            AbstractMap.SimpleEntry("foo", "bar")
        )
        assertThat(updatedRequiredAction.defaultAction).isEqualTo(true)
        assertThat(updatedRequiredAction.enabled).isEqualTo(requiredAction.enabled)
        assertThat(updatedRequiredAction.priority).isEqualTo(321)
    }

    @Test
    fun testUpdateRequiredActions_Rollback() {
        val requiredAction = client.requiredAction(testRealm, "UPDATE_PASSWORD")
        val action = UpdateRequiredActionAction(
            testRealm, requiredAction.alias, null, "update_password2", "Update password 2",
            mapOf(
                "foo" to "bar",
                "foo3" to "bar3"
            ), true, null, 321
        )

        action.executeIt()

        Assertions.assertThatThrownBy {
            client.requiredAction(testRealm, "UPDATE_PASSWORD")
        }.isInstanceOf(FeignException::class.java).hasMessageContaining("404")

        val updatedRequiredAction = client.requiredAction(testRealm, "update_password2")
        assertThat(updatedRequiredAction).isNotNull()
        assertThat(updatedRequiredAction.alias).isEqualTo("update_password2")
        assertThat(updatedRequiredAction.providerId).isEqualTo(requiredAction.providerId)
        assertThat(updatedRequiredAction.name).isEqualTo("Update password 2")
        assertThat(updatedRequiredAction.config).containsExactly(
            AbstractMap.SimpleEntry("foo3", "bar3"),
            AbstractMap.SimpleEntry("foo", "bar")
        )
        assertThat(updatedRequiredAction.defaultAction).isEqualTo(true)
        assertThat(updatedRequiredAction.enabled).isEqualTo(requiredAction.enabled)
        assertThat(updatedRequiredAction.priority).isEqualTo(321)

        action.undoIt()

        Assertions.assertThatThrownBy {
            client.requiredAction(testRealm, "update_password2")
        }.isInstanceOf(FeignException::class.java).hasMessageContaining("404")

        val revertedRequiredAction = client.requiredAction(testRealm, "UPDATE_PASSWORD")
        assertThat(revertedRequiredAction).isNotNull()
        assertThat(revertedRequiredAction.alias).isEqualTo("UPDATE_PASSWORD")
        assertThat(revertedRequiredAction.providerId).isEqualTo("UPDATE_PASSWORD")
        assertThat(revertedRequiredAction.name).isEqualTo("Update Password")
        assertThat(revertedRequiredAction.config).isEmpty()
        assertThat(revertedRequiredAction.defaultAction).isEqualTo(false)
        assertThat(revertedRequiredAction.enabled).isEqualTo(true)
        assertThat(revertedRequiredAction.priority).isEqualTo(30)
    }

}
