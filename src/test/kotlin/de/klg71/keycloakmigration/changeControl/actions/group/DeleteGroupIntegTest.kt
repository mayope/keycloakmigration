package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.existsGroup
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class DeleteGroupIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteGroup() {
        AddGroupAction(testRealm, "integrationTest").executeIt()
        DeleteGroupAction(testRealm, "integrationTest").executeIt()

        assertThat(client.existsGroup("integrationTest", testRealm)).isFalse()
    }

    @Test
    fun testDeleteGroupNotExisting() {
        assertThatThrownBy {
            DeleteGroupAction(testRealm, "integrationTest").executeIt()
        }.isInstanceOf(KeycloakApiException::class.java)
            .hasMessage("Group with name: integrationTest does not exist in realm: ${testRealm}!")
    }
}
