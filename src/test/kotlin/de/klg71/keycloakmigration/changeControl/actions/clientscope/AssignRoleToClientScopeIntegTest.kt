package de.klg71.keycloakmigration.changeControl.actions.clientscope

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.UpdateClientAction
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import de.klg71.keycloakmigration.keycloakapi.clientScopeByName
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.*

class AssignRoleToClientScopeIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val scopeName = "integrationTest"
    private val clientId = "integrationTest"
    private val roleName = "testRole"


    @Test
    fun testAssignClientRole() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        AddSimpleClientAction(testRealm, clientId).executeIt()
        UpdateClientAction(testRealm, clientId).executeIt()
        AddRoleAction(testRealm, roleName, clientId).executeIt()

        AssignRoleToClientScopeAction(testRealm, scopeName, roleName, clientId).executeIt()

        val testRole = RoleListItem(
            UUID.randomUUID(),
            roleName,
            null,
            false,
            true,
            client.clientUUID(clientId, testRealm).toString()
        )

        client.clientScopeClientRoles(
            testRealm,
            client.clientScopeByName(scopeName, testRealm).id,
            client.clientUUID(clientId, testRealm)
        ).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").contains(testRole)
        }
    }

    @Test
    fun testAssignRealmRole() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        AddRoleAction(testRealm, roleName).executeIt()

        AssignRoleToClientScopeAction(testRealm, scopeName, roleName).executeIt()

        val testRole = RoleListItem(UUID.randomUUID(), roleName, null, false, false, testRealm)

        client.clientScopeRealmRoles(testRealm, client.clientScopeByName(scopeName, testRealm).id).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").contains(testRole)
        }
    }

    @Test
    fun testAssignRole_clientScopeNotExisting() {
        AddRoleAction(testRealm, roleName).executeIt()
        assertThatThrownBy {
            AssignRoleToClientScopeAction(testRealm, scopeName, roleName, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("ClientScope with name: $scopeName does not exist in realm: $testRealm!")
    }

    @Test
    fun testAssignRole_clientRoleNotExisting() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddClientScopeAction(testRealm, scopeName).executeIt()
        assertThatThrownBy {
            AssignRoleToClientScopeAction(testRealm, scopeName, roleName, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Role with name: $roleName in client: $scopeName does not exist in realm: $testRealm!")
    }

    @Test
    fun testAssignRole_realmRoleNotExisting() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        assertThatThrownBy {
            AssignRoleToClientScopeAction(testRealm, scopeName, roleName).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Role with name: $roleName does not exist in realm: $testRealm!")
    }

    @Test
    fun testAssignClientRole_undo() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddRoleAction(testRealm, roleName, clientId).executeIt()
        AssignRoleToClientScopeAction(testRealm, scopeName, roleName, clientId).run {
            executeIt()
            undoIt()
        }

        val roles = client.clientScopeClientRoles(
            testRealm,
            client.clientScopeByName(scopeName, testRealm).id,
            client.clientById(clientId, testRealm).id
        )
        assertThat(roles.any { it.name == roleName }).isFalse
    }

    @Test
    fun testAssignRealmRole_undo() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        AddRoleAction(testRealm, roleName).executeIt()
        AssignRoleToClientScopeAction(testRealm, scopeName, roleName).run {
            executeIt()
            undoIt()
        }

        val roles = client.clientScopeRealmRoles(testRealm, client.clientScopeByName(scopeName, testRealm).id)
        assertThat(roles.any { it.name == roleName }).isFalse
    }
}
