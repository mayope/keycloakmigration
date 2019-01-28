package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.RoleListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.roleExistsByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.standalone.inject
import java.util.*

class DeleteRoleIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testDeleteRole() {
        AddRoleAction("master", "integrationTest").executeIt()
        DeleteRoleAction("master", "integrationTest").executeIt()

        RoleListItem(UUID.randomUUID(), "integrationTest", "", false, false, "master").let {
            assertThat(client.roleExistsByName("integrationTest", "master")).isFalse()
        }
    }

    @Test
    fun testDeleteRoleNotExisting() {
        assertThatThrownBy {
            DeleteRoleAction("master", "integrationTest").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Role with name: integrationTest does not exist in realm: master!")

    }
}