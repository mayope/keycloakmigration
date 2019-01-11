package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.GroupListItem
import de.klg71.keycloakmigration.model.UserListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject
import java.util.*

class AddGroupIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddGroup() {
        AddGroupAction("master", "integrationTest").executeIt()

        GroupListItem(UUID.randomUUID(), "integrationTest", "integrationTest", listOf()).let {
            assertThat(client.searchGroup("integrationTest","master")).hasSize(1).usingElementComparatorOnFields("name").contains(it)
        }
    }

    @Test
    fun testAddGroupAlreadyExisting() {
        AddGroupAction("master", "integrationTest").executeIt()
        assertThatThrownBy {
            AddGroupAction("master", "integrationTest").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Group with name: integrationTest already exists in realm: master!")
    }

    @After
    fun cleanup() {
        DeleteGroupAction("master", "integrationTest").executeIt()
    }
}