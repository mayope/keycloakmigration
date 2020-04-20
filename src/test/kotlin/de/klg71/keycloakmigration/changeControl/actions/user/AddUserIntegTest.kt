package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.group.AddGroupAction
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.model.UserListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.clientUUID
import de.klg71.keycloakmigration.rest.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject
import java.util.*

class AddUserIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddUser() {
        AddUserAction(testRealm, "test").executeIt()

        UserListItem(UUID.randomUUID(), 0L, "test", enabled = true, emailVerified = true).let {
            assertThat(client.users(testRealm)).usingElementComparatorOnFields("username", "enabled", "emailVerified")
                    .contains(it)
        }
    }

    @Test
    fun testAddUser_realmDoesNotExist() {
        assertThatThrownBy {
            AddUserAction("doesNotExist", "test").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Realm with id: doesNotExist does not exist!")
    }

    @Test
    fun testAddUser_withRolesAndGroups() {
        AddGroupAction(testRealm, "testGroup").executeIt()
        AddSimpleClientAction(testRealm, "testClient").executeIt()
        AddRoleAction(testRealm, "testClientRole", "testClient").executeIt()
        AddRoleAction(testRealm, "testRealmRole").executeIt()
        AddUserAction(testRealm, "test",
                groups = listOf("testGroup"),
                realmRoles = listOf("testRealmRole"),
                clientRoles = listOf(ClientRole("testClientRole", "testClient"))).executeIt()

        val user = client.userByName(testRealm,"test")
        assertThat(client.userGroups(testRealm, user.id).map { it.name }).containsOnly("testGroup")
        assertThat(client.userRoles(testRealm, user.id).map { it.name }).contains("testRealmRole")
        assertThat(client.userClientRoles(testRealm, user.id, client.clientUUID("testClient", testRealm)).map { it.name }).containsOnly("testClientRole")
    }

    @Test
    fun testAddUser_withEmail() {
        val email = "test@example.com"
        AddUserAction(testRealm, "test", email = email).executeIt()

        val user = client.userByName(testRealm,"test")
        assertThat(user.email).isEqualTo(email)
    }
}
