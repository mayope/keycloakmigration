package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.groupByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class RenameGroupIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testRenameGroup() {
        AddGroupAction(testRealm, "integrationTest").executeIt()
        val createdGroup = client.groupByName("integrationTest", testRealm)

        RenameGroupAction(testRealm, "integrationTest", "newName").executeIt()

        assertThat(client.groupByName("newName", testRealm).id).isEqualTo(createdGroup.id)
        assertThatThrownBy { client.groupByName("integrationTest", testRealm) }
            .hasMessage("Group with name: integrationTest does not exist in realm: $testRealm!")
            .isInstanceOf(KeycloakApiException::class.java)
    }

    @Test
    fun testUndoRenameGroup() {
        AddGroupAction(testRealm, "integrationTest").executeIt()
        val createdGroup = client.groupByName("integrationTest", testRealm)

        val renameGroupAction = RenameGroupAction(testRealm, "integrationTest", "newName")
        renameGroupAction.executeIt()

        renameGroupAction.undoIt()

        assertThat(client.groupByName("integrationTest", testRealm).id).isEqualTo(createdGroup.id)
        assertThatThrownBy { client.groupByName("newName", testRealm) }
            .hasMessage("Group with name: newName does not exist in realm: $testRealm!")
            .isInstanceOf(KeycloakApiException::class.java)
    }
}
