package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.RoleListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject
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
}