package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.*

class AssignRoleIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testAssignRole() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        AddRoleAction(testRealm, "testRole").executeIt()
        AssignRoleAction(testRealm, "testRole", "testIntegration").executeIt()

        val testRole = RoleListItem(UUID.randomUUID(), "testRole", null, false, false, testRealm)

        client.userRoles(testRealm, client.userByName("testIntegration", testRealm).id).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").contains(testRole)
        }
    }

    @Test
    fun testAssignRole_userNotExisting() {
        AddRoleAction(testRealm, "testRole").executeIt()
        assertThatThrownBy {
            AssignRoleAction(testRealm, "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("User with name: testIntegration does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testAssignRole_roleNotExisting() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        assertThatThrownBy {
            AssignRoleAction(testRealm, "testRole", "testIntegration").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Role with name: testRole does not exist in realm: ${testRealm}!")
    }
}
