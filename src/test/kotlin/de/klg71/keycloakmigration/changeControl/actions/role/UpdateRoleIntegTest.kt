package de.klg71.keycloakmigration.changeControl.actions.role

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientRoleByName
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.*

class UpdateRoleIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val clientId = "integrationTest"
    private val roleName = "testRole"
    private val roleDescription = "testRoleDescription"


    @Test
    fun testUpdateRealmRole() {
        AddRoleAction(testRealm, roleName).executeIt()

        UpdateRoleAction(testRealm, roleName, description = roleDescription, composite = true).executeIt()

        RoleListItem(UUID.randomUUID(), roleName, roleDescription, false, false, testRealm).let {
            assertThat(client.roleByName(roleName, testRealm)).isEqualToComparingOnlyGivenFields(
                it, "name", "description", "clientRole", "composite", "containerId"
            )
        }
    }

    @Test
    fun testUpdateClientRole() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddRoleAction(testRealm, roleName, clientId).executeIt()

        UpdateRoleAction(testRealm, roleName, clientId, description = roleDescription, clientRole = true).executeIt()

        RoleListItem(
            UUID.randomUUID(),
            roleName,
            roleDescription,
            false,
            true,
            client.clientUUID(clientId, testRealm).toString()
        ).let {
            assertThat(client.clientRoleByName(roleName, clientId, testRealm)).isEqualToComparingOnlyGivenFields(
                it, "name", "description", "clientRole", "composite", "containerId"
            )
        }
    }

    @Test
    fun testUpdateRole_realmRoleNotExisting() {
        assertThatThrownBy {
            UpdateRoleAction(testRealm, roleName, description = roleDescription, composite = true).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Role with name: $roleName does not exist in realm: $testRealm!")
    }

    @Test
    fun testUpdateRole_clientRoleNotExisting() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        assertThatThrownBy {
            UpdateRoleAction(testRealm, roleName, clientId, description = roleDescription, composite = true).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("ClientRole with name: $roleName does not exist in realm: $testRealm!")
    }

    @Test
    fun testUpdateRole_clientNotExisting() {
        assertThatThrownBy {
            UpdateRoleAction(testRealm, roleName, clientId, description = roleDescription, composite = true).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Client with id: $clientId does not exist in realm: $testRealm!")
    }

    @Test
    fun testAddCompositeRoles() {
        AddSimpleClientAction(testRealm, clientId, true).executeIt()
        AddRoleAction(testRealm, "integrationTestChildRole1").executeIt()
        AddRoleAction(testRealm, "integrationTestChildRole2", clientId).executeIt()
        AddRoleAction(testRealm, roleName).executeIt()
        UpdateRoleAction(
            testRealm, roleName, composite = true, compositeChildRoles = listOf(
                RoleSelector(name = "integrationTestChildRole1"),
                RoleSelector(name = "integrationTestChildRole2", clientId)
            )
        ).executeIt()

        val role = client.roleByName(roleName, testRealm)

        RoleListItem(UUID.randomUUID(), roleName, "", true, false, testRealm).let {
            assertThat(role).isEqualToComparingOnlyGivenFields(
                it, "name", "description", "clientRole", "composite", "containerId"
            )
        }

        assertThat(client.getCompositeChildRoles(role.id, testRealm).map { it.name }).containsExactlyInAnyOrder(
            "integrationTestChildRole1", "integrationTestChildRole2"
        )
    }
}
