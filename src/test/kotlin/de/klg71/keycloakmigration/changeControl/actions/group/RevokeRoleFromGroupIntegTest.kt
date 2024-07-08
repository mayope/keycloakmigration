package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import de.klg71.keycloakmigration.keycloakapi.groupByName
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.UUID

class RevokeRoleFromGroupIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val clientId = "clientId"
    private val role = "testRole"
    private val group = "testIntegration"


    @Test
    fun testRevokeRole() {
        AddGroupAction(testRealm, group).executeIt()
        AddRoleAction(testRealm, role).executeIt()
        AssignRoleToGroupAction(testRealm, role, group).executeIt()
        RevokeRoleFromGroupAction(testRealm, role, group).executeIt()

        val role = RoleListItem(UUID.randomUUID(), role, null, false, false, testRealm)

        client.groupRoles(testRealm, client.groupByName(group, testRealm).id).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").doesNotContain(role)
        }
    }

    @Test
    fun testRevokeClientRole() {
        AddGroupAction(testRealm, group).executeIt()
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddRoleAction(testRealm, role, clientId).executeIt()
        AssignRoleToGroupAction(testRealm, role, group, clientId).executeIt()
        RevokeRoleFromGroupAction(testRealm, role, group, clientId).executeIt()

        val role = RoleListItem(UUID.randomUUID(), role, null, false, false,
            client.clientById(clientId, testRealm).id.toString())

        client.groupClientRoles(
            testRealm,
            client.groupByName(group, testRealm).id,
            client.clientById(clientId, testRealm).id
        ).let {
            assertThat(it).usingElementComparatorOnFields("name", "containerId").doesNotContain(role)
        }
    }

    @Test
    fun testRevokeRole_groupNotExisting() {
        AddRoleAction(testRealm, role).executeIt()
        assertThatThrownBy {
            RevokeRoleFromGroupAction(testRealm, role, group).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Group with name: $group does not exist in realm: $testRealm!")
    }

    @Test
    fun testAssignRole_realmRoleNotExisting() {
        AddGroupAction(testRealm, group).executeIt()
        assertThatThrownBy {
            RevokeRoleFromGroupAction(testRealm, role, group).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Role with name: $role does not exist in realm: $testRealm!")
    }

    @Test
    fun testAssignRole_clientRoleNotExisting() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddGroupAction(testRealm, group).executeIt()
        assertThatThrownBy {
            RevokeRoleFromGroupAction(testRealm, role, group, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Role with name: $role in client: $clientId does not exist in realm: $testRealm!")
    }


    @Test
    fun testAssignRole_roleNotAssigned() {
        AddGroupAction(testRealm, group).executeIt()
        AddRoleAction(testRealm, role).executeIt()
        assertThatThrownBy {
            RevokeRoleFromGroupAction(testRealm, role, group).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Group with name: $group in realm: $testRealm does not have role: $role!")
    }

    @Test
    fun testAssignRole_clientRoleNotAssigned() {
        AddGroupAction(testRealm, group).executeIt()
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddRoleAction(testRealm, role, clientId).executeIt()
        assertThatThrownBy {
            RevokeRoleFromGroupAction(testRealm, role, group, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Group with name: $group in client: $clientId in realm: $testRealm does not have role: $role!")
    }
}
