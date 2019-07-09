package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.changeControl.actions.role.DeleteRoleAction
import de.klg71.keycloakmigration.model.RoleListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.groupByName
import de.klg71.keycloakmigration.rest.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject
import java.util.*

class RevokeRoleFromGroupIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testRevokeRole() {
        AddGroupAction(testRealm, "testIntegration").executeIt()
        AddRoleAction(testRealm, "testRole").executeIt()
        AssignRoleToGroupAction(testRealm, "testRole", "testIntegration").executeIt()
        RevokeRoleFromGroupAction(testRealm, "testRole", "testIntegration").executeIt()

        val testRole = RoleListItem(UUID.randomUUID(), "testRole", null, false, false, testRealm)

        client.groupRoles(testRealm, client.groupByName("testIntegration", testRealm).id).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").doesNotContain(testRole)
        }
    }

    @Test
    fun testRevokeRole_userNotExisting() {
        AddRoleAction(testRealm, "testRole").executeIt()
        assertThatThrownBy {
            RevokeRoleFromGroupAction(testRealm, "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Group with name: testIntegration does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testAssignRole_roleNotExisting() {
        AddGroupAction(testRealm, "testIntegration").executeIt()
        assertThatThrownBy {
            RevokeRoleFromGroupAction(testRealm, "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Role with name: testRole does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testAssignRole_roleNotAssigned() {
        AddGroupAction(testRealm, "testIntegration").executeIt()
        AddRoleAction(testRealm, "testRole").executeIt()
        assertThatThrownBy {
            RevokeRoleFromGroupAction(testRealm, "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Group with name: testIntegration in realm: ${testRealm} does not have role: testRole!")
    }
}