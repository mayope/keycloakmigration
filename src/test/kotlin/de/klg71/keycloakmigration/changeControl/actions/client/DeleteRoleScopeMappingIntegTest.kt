package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.UUID

class DeleteRoleScopeMappingIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val clientId = "testIntegration"
    private val role = "testRole"

    @Test
    fun testDeleteRealmRoleScopeMapping() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddRoleAction(testRealm, role).executeIt()
        AddRoleScopeMappingAction(testRealm, role, clientId).executeIt()

        DeleteRoleScopeMappingAction(testRealm, role, clientId).executeIt()

        client.realmRoleScopeMappingsOfClient(testRealm, client.clientUUID(clientId, testRealm)).let {
            assertThat(it).isEmpty()
        }
    }

    @Test
    fun testDeleteClientRoleScopeMapping() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        val roleClientId = "testRoleClient"
        AddSimpleClientAction(testRealm, roleClientId).executeIt()
        AddRoleAction(testRealm, role,clientId= roleClientId).executeIt()
        AddRoleScopeMappingAction(testRealm, role, clientId, roleClientId).executeIt()

        DeleteRoleScopeMappingAction(testRealm, role, clientId, roleClientId).executeIt()

        client.clientRoleScopeMappingsOfClient(testRealm, client.clientUUID(clientId, testRealm), client.clientUUID(roleClientId,testRealm)).let {
            assertThat(it).isEmpty()
        }
    }

    @Test
    fun testUndoDeleteRealmRoleScopeMapping() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddRoleAction(testRealm, role).executeIt()

        AddRoleScopeMappingAction(testRealm, role, clientId).executeIt()
        val deleteRoleScopeMappingAction = DeleteRoleScopeMappingAction(testRealm, role, clientId)
        deleteRoleScopeMappingAction.executeIt()

        deleteRoleScopeMappingAction.undoIt()

        val testRoleScopeMapping = RoleListItem(UUID.randomUUID(), role, null, false, false, testRealm)

        client.realmRoleScopeMappingsOfClient(testRealm, client.clientUUID(clientId, testRealm)).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").contains(testRoleScopeMapping)
        }
    }

    @Test
    fun testUndoDeleteClientRoleScopeMapping() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        val roleClientId = "testRoleClient"
        AddSimpleClientAction(testRealm, roleClientId).executeIt()
        AddRoleAction(testRealm, role,clientId= roleClientId).executeIt()

        AddRoleScopeMappingAction(testRealm, role, clientId, roleClientId).executeIt()
        val deleteRoleScopeMappingAction = DeleteRoleScopeMappingAction(testRealm, role, clientId, roleClientId)
        deleteRoleScopeMappingAction.executeIt()

        deleteRoleScopeMappingAction.undoIt()

        val testRoleScopeMapping = RoleListItem(
            UUID.randomUUID(), role, null, false, true,
            client.clientUUID(roleClientId,testRealm).toString())

        client.clientRoleScopeMappingsOfClient(testRealm, client.clientUUID(clientId, testRealm), client.clientUUID(roleClientId,testRealm)).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").contains(testRoleScopeMapping)
        }
    }

    @Test
    fun testDeleteRoleScopeMapping_clientNotExisting() {
        AddRoleAction(testRealm, role).executeIt()
        assertThatThrownBy {
            AssignRoleToClientAction(testRealm, role, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Client with name: $clientId does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testDeleteRoleScopeMapping_roleNotExisting() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        assertThatThrownBy {
            AssignRoleToClientAction(testRealm, role, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Role with name: $role does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testDeleteRoleScopeMapping_clientRoleNotExisting() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddSimpleClientAction(testRealm, "testRoleClient").executeIt()
        assertThatThrownBy {
            AssignRoleToClientAction(testRealm, role, clientId, "testRoleClient").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Role with name: $role in client: testRoleClient does not exist in realm: $testRealm!")
    }

}
