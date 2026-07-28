package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import feign.FeignException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.UUID

class RevokeRoleFromClientIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val clientId = "clientId"
    private val role = "testRole"
    private val roleClientId = "roleClientId"

    @Test
    fun testRevokeClientRole() {
        AddSimpleClientAction(testRealm, clientId, serviceAccountsEnabled = true).executeIt()
        AddRoleAction(testRealm, role, clientRole = true).executeIt()
        AssignRoleToClientAction(testRealm, role, clientId).executeIt()
        RevokeRoleFromClientAction(testRealm, role, clientId).executeIt()

        val role = RoleListItem(
            UUID.randomUUID(), role, null,
            composite = false,
            clientRole = false,
            containerId = testRealm
        )

        val actual = client.clientRoles(testRealm, client.clientById(clientId, testRealm).id)
        assertThat(actual).doesNotContain(role);
    }

    @Test
    fun testRevokeRole_clientNotExisting() {
        AddRoleAction(testRealm, role).executeIt()
        assertThatThrownBy {
            RevokeRoleFromClientAction(testRealm, role, clientId, roleClientId).executeIt()
        }.isInstanceOf(KeycloakApiException::class.java)
            .hasMessage("Client with id: $roleClientId does not exist in realm: $testRealm!")
    }

   @Test
   fun testRevokeRole_clientRoleDoesNotExist() {
       assertThatThrownBy {
           RevokeRoleFromClientAction(testRealm, role, clientId).executeIt()
       }.isInstanceOf(FeignException.NotFound::class.java)
           .hasMessageContaining("Could not find role")
   }

    @Test
    fun testRevokeRole_clientRoleNotAssignedToClient() {
        AddSimpleClientAction(testRealm, roleClientId, serviceAccountsEnabled = true).executeIt()
        AddRoleAction(testRealm, role).executeIt()
        assertThatThrownBy {
            RevokeRoleFromClientAction(testRealm, role, clientId, roleClientId).executeIt()
        }.isInstanceOf(KeycloakApiException::class.java)
            .hasMessageContaining("Role with name: $role does not exist on client $roleClientId on realm $testRealm!")
    }

    @Test
    fun testRevokeRole_realmRoleDoesNotExist() {
        AddSimpleClientAction(testRealm, roleClientId, serviceAccountsEnabled = true).executeIt()
        assertThatThrownBy {
            RevokeRoleFromClientAction(testRealm, role, clientId, roleClientId).executeIt()
        }.isInstanceOf(KeycloakApiException::class.java)
            .hasMessageContaining("Role with name: $role does not exist on client $roleClientId on realm $testRealm!")
    }

    @Test
    fun testRevokeRole_realmRoleNotAssignedToClient() {
        AddSimpleClientAction(testRealm, roleClientId, serviceAccountsEnabled = true).executeIt()
        AddRoleAction(testRealm, role).executeIt()
        assertThatThrownBy {
            RevokeRoleFromClientAction(testRealm, role, clientId, roleClientId).executeIt()
        }.isInstanceOf(KeycloakApiException::class.java)
            .hasMessage("Role with name: $role does not exist on client $roleClientId on realm $testRealm!")
    }


}
