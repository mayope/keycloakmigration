package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.group.AddGroupAction
import de.klg71.keycloakmigration.keycloakapi.model.UserGroupListItem
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject
import java.util.*

class RevokeGroupIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testRevokeRole() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        AddGroupAction(testRealm, "testGroup").executeIt()
        AssignGroupAction(testRealm, "testIntegration", "testGroup").executeIt()
        RevokeGroupAction(testRealm, "testIntegration", "testGroup").executeIt()

        val testGroup = UserGroupListItem(UUID.randomUUID(), "testGroup", "/testGroup")

        client.userGroups(testRealm, client.userByName("testIntegration", testRealm).id).let {
            assertThat(it).usingElementComparatorOnFields("name", "path").doesNotContain(testGroup)
        }
    }

    @Test
    fun testRevokeGroup_userNotExisting() {
        AddGroupAction(testRealm, "testGroup").executeIt()
        assertThatThrownBy {
            RevokeGroupAction(testRealm, "testIntegration", "testGroup").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("User with name: testIntegration does not exist in realm: $testRealm!")
    }

    @Test
    fun testAssignGroup_groupNotExisting() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        assertThatThrownBy {
            RevokeGroupAction(testRealm, "testIntegration", "testGroup").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Group with name: testGroup does not exist in realm: $testRealm!")
    }

    @Test
    fun testAssignGroup_groupNotAssigned() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        AddGroupAction(testRealm, "testGroup").executeIt()
        assertThatThrownBy {
            RevokeGroupAction(testRealm, "testIntegration", "testGroup").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("User with name: testIntegration in realm: ${testRealm} does not have group: testGroup!")
    }
}
