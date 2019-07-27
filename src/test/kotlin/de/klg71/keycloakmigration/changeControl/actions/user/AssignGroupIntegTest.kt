package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.group.AddGroupAction
import de.klg71.keycloakmigration.model.UserGroupListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject
import java.util.*

class AssignGroupIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testAssignGroup() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        AddGroupAction(testRealm, "testGroup").executeIt()
        AssignGroupAction(testRealm, "testIntegration", "testGroup").executeIt()

        val testGroup = UserGroupListItem(UUID.randomUUID(), "testGroup", "/testGroup")

        client.userGroups(testRealm, client.userByName("testIntegration", testRealm).id).let {
            assertThat(it).usingElementComparatorOnFields("name", "path").contains(testGroup)
        }
    }

    @Test
    fun testAssignGroup_userNotExisting() {
        AddGroupAction(testRealm, "testGroup").executeIt()
        assertThatThrownBy {
            AssignGroupAction(testRealm, "testIntegration", "testGroup").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("User with name: testIntegration does not exist in realm: $testRealm!")
    }

    @Test
    fun testAssignGroup_roleNotExisting() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        assertThatThrownBy {
            AssignGroupAction(testRealm, "testIntegration", "testGroup").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Group with name: testGroup does not exist in realm: $testRealm!")
    }
}