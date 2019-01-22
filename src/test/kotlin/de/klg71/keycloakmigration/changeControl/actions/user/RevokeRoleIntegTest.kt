package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.changeControl.actions.role.DeleteRoleAction
import de.klg71.keycloakmigration.model.RoleListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject
import java.util.*

class RevokeRoleIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testRevokeRole() {
        AddUserAction("master", "testIntegration").executeIt()
        AddRoleAction("master", "testRole").executeIt()
        AssignRoleAction("master", "testRole", "test").executeIt()
        RevokeRoleAction("master", "testRole", "test").executeIt()

        val testRole = RoleListItem(UUID.randomUUID(), "testRole", null, false, false, "master")

        client.userRoles("master", client.userByName("testIntegration", "master").id).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").doesNotContain(testRole)
        }
    }

    @Test
    fun testRevokeRole_userNotExisting() {
        AddRoleAction("master", "testRole").executeIt()
        assertThatThrownBy {
            RevokeRoleAction("master", "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("User with name: testIntegration does not exist in realm: master!")
    }

    @Test
    fun testAssignRole_roleNotExisting() {
        AddUserAction("master", "testIntegration").executeIt()
        assertThatThrownBy {
            RevokeRoleAction("master", "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Role with name: testRole does not exist in realm: master!")
    }

    @Test
    fun testAssignRole_roleNotAssigned() {
        AddUserAction("master", "testIntegration").executeIt()
        AddRoleAction("master", "testRole").executeIt()
        assertThatThrownBy {
            RevokeRoleAction("master", "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("User with name: testIntegration in realm: master does not have role: testRole!")
    }

    @After
    fun cleanup() {
        try {
            RevokeRoleAction("master", "testRole", "test").executeIt()
        } catch (t: Throwable) {
        }
        try {
            DeleteUserAction("master", "testIntegration").executeIt()
        } catch (t: Throwable) {
        }
        try {
            DeleteRoleAction("master", "testRole").executeIt()
        } catch (t: Throwable) {
        }
    }
}