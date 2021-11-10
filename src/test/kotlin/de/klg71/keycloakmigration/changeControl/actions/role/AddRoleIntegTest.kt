package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.*

class AddRoleIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testAddRole() {
        AddRoleAction(testRealm, "integrationTest").executeIt()

        RoleListItem(UUID.randomUUID(), "integrationTest", "", false, false, testRealm).let {
            assertThat(client.roleByName("integrationTest", testRealm)).isEqualToComparingOnlyGivenFields(it, "name", "description", "clientRole", "composite", "containerId")
        }
    }

    @Test
    fun testAddRoleAlreadyExisting() {
        AddRoleAction(testRealm, "integrationTest").executeIt()
        assertThatThrownBy {
            AddRoleAction(testRealm, "integrationTest").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Role with name: integrationTest already exists in realm: ${testRealm}!")

    }

    @Test
    fun testAddCompositeRoles() {
        AddSimpleClientAction(testRealm, "simpleClient", true).executeIt()
        AddRoleAction(testRealm, "integrationTestChildRole1").executeIt()
        AddRoleAction(testRealm, "integrationTestChildRole2", clientId="simpleClient").executeIt()
        AddRoleAction(testRealm, "integrationTest",composite = true, compositeChildRoles = listOf(
                RoleSelector(name = "integrationTestChildRole1"),
                RoleSelector(name = "integrationTestChildRole2", clientId = "simpleClient")
        )).executeIt()

        val role = client.roleByName("integrationTest", testRealm)

        RoleListItem(UUID.randomUUID(), "integrationTest", "", true, false, testRealm).let {
            assertThat(role).isEqualToComparingOnlyGivenFields(it, "name", "description", "clientRole", "composite", "containerId")
        }

        assertThat(client.getCompositeChildRoles(role.id, testRealm).map { it.name }).containsExactlyInAnyOrder(
                "integrationTestChildRole1", "integrationTestChildRole2"
        )
    }
}
