package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import de.klg71.keycloakmigration.keycloakapi.roleExistsByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.UUID

class DeleteRoleIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testDeleteRole() {
        AddRoleAction(testRealm, "integrationTest").executeIt()
        DeleteRoleAction(testRealm, "integrationTest").executeIt()

        RoleListItem(UUID.randomUUID(), "integrationTest", "", false, false, testRealm).let {
            assertThat(client.roleExistsByName("integrationTest", testRealm)).isFalse()
        }
    }

    @Test
    fun testDeleteRoleNotExisting() {
        assertThatThrownBy {
            DeleteRoleAction(testRealm, "integrationTest").executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Role with name: integrationTest does not exist in realm: ${testRealm}!")

    }
}
