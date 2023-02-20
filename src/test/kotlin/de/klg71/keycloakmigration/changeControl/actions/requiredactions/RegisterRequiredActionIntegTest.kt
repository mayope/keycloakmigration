package de.klg71.keycloakmigration.changeControl.actions.requiredactions

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class RegisterRequiredActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testRegisterRequiredAction() {
        val providerId = "webauthn-register-passwordless"
        RegisterRequiredActionAction(
            testRealm, "webauthn-register-passwordless", providerId
        ).executeIt()


        val updatedRequiredAction = client.requiredAction(testRealm, providerId)
        assertThat(updatedRequiredAction).isNotNull()
        assertThat(updatedRequiredAction.providerId).isEqualTo(providerId)
        assertThat(updatedRequiredAction.name).isEqualTo("webauthn-register-passwordless")
        assertThat(updatedRequiredAction.enabled).isEqualTo(true)
    }

    @Test
    fun testRegisterRequiredActionUndo() {
        val providerId = "webauthn-register"
        val action = RegisterRequiredActionAction(
            testRealm, "webauthn-register", providerId
        )
        action.executeIt()
        action.undoIt()


        val updatedRequiredAction = client.requiredAction(testRealm, providerId)
        assertThat(updatedRequiredAction).isNotNull()
        assertThat(updatedRequiredAction.providerId).isEqualTo(providerId)
        assertThat(updatedRequiredAction.name).isEqualTo("webauthn-register")
        assertThat(updatedRequiredAction.enabled).isEqualTo(false)
    }

}
