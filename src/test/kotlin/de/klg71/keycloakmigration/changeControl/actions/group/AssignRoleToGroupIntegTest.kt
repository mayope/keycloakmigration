package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.model.RoleListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.groupByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject
import java.util.*

class AssignRoleToGroupIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testAssignRole() {
        AddGroupAction(testRealm, "testIntegration").executeIt()
        AddRoleAction(testRealm, "testRole").executeIt()
        AssignRoleToGroupAction(testRealm, "testRole", "testIntegration").executeIt()

        val testRole = RoleListItem(UUID.randomUUID(), "testRole", null, false, false, testRealm)

        client.groupRoles(testRealm, client.groupByName("testIntegration", testRealm).id).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").contains(testRole)
        }
    }

    @Test
    fun testAssignRole_groupNotExisting() {
        AddRoleAction(testRealm, "testRole").executeIt()
        assertThatThrownBy {
            AssignRoleToGroupAction(testRealm, "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Group with name: testIntegration does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testAssignRole_roleNotExisting() {
        AddGroupAction(testRealm, "testIntegration").executeIt()
        assertThatThrownBy {
            AssignRoleToGroupAction(testRealm, "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Role with name: testRole does not exist in realm: ${testRealm}!")
    }
}