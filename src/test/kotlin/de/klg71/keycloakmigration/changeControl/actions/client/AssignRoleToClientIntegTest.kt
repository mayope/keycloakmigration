package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.model.RoleListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject
import java.util.UUID

class AssignRoleToClientIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val clientId = "testIntegration"
    private val role = "testRole"

    @Test
    fun testAssignRole() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        UpdateClientAction(testRealm, clientId, serviceAccountsEnabled = true).executeIt()
        AddRoleAction(testRealm, role).executeIt()

        AssignRoleToClientAction(testRealm, role, clientId).executeIt()

        val serviceAccount = client.clientServiceAccount(client.clientUUID(clientId, testRealm), testRealm)

        val testRole = RoleListItem(UUID.randomUUID(), role, null, false, false, testRealm)

        client.userRoles(testRealm, serviceAccount.id).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").contains(testRole)
        }
    }

    @Test
    fun testAssignClientRole() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        UpdateClientAction(testRealm, clientId, serviceAccountsEnabled = true).executeIt()
        val roleClientId = "testRoleClient"
        AddSimpleClientAction(testRealm, roleClientId).executeIt()
        AddRoleAction(testRealm, role,clientId= roleClientId).executeIt()

        AssignRoleToClientAction(testRealm, role, clientId, roleClientId).executeIt()

        val serviceAccount = client.clientServiceAccount(client.clientUUID(clientId, testRealm), testRealm)

        val testRole = RoleListItem(UUID.randomUUID(), role, null, false, true,
                client.clientUUID(roleClientId,testRealm).toString())

        client.userClientRoles(testRealm, serviceAccount.id,client.clientUUID(roleClientId,testRealm)).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").contains(testRole)
        }
    }

    @Test
    fun testAssignRole_clientNotExisting() {
        AddRoleAction(testRealm, role).executeIt()
        assertThatThrownBy {
            AssignRoleToClientAction(testRealm, role, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Client with name: $clientId does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testAssignRole_roleNotExisting() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        assertThatThrownBy {
            AssignRoleToClientAction(testRealm, role, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Role with name: $role does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testAssignRole_clientRoleNotExisting() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddSimpleClientAction(testRealm, "testRoleClient").executeIt()
        assertThatThrownBy {
            AssignRoleToClientAction(testRealm, role, clientId, "testRoleClient").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Role with name: $role in client: testRoleClient does not exist in realm: $testRealm!")
    }

    @Test
    fun testAssignRole_clientServiceAccountDisabled() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddRoleAction(testRealm, role).executeIt()
        assertThatThrownBy {
            AssignRoleToClientAction(testRealm, role, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Service account not enabled for client: $clientId!")
    }
}
