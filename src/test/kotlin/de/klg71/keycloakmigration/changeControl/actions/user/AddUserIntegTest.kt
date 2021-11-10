package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.group.AddGroupAction
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.User
import de.klg71.keycloakmigration.keycloakapi.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.UUID

class AddUserIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddUser() {
        AddUserAction(
            testRealm, name = "test", email = "local@local.local", firstName = "test", lastName = "user"
        ).executeIt()

        User(
            id = UUID.randomUUID(), createdTimestamp = 0L, username = "test", enabled = true, emailVerified = true,
            notBefore = 1L, totp = false, requiredActions = emptyList(), attributes = null, access = null,
            disableableCredentialTypes = emptyList(), email = "local@local.local", firstName = "test",
            lastName = "user", credentials = emptyList()
        ).let {
            assertThat(client.users(testRealm)).usingElementComparatorOnFields(
                "username", "enabled", "emailVerified",
                "email", "firstName", "lastName"
            ).contains(it)
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
        AddUserAction(
            testRealm, "test",
            groups = listOf("testGroup"),
            realmRoles = listOf("testRealmRole"),
            clientRoles = listOf(ClientRole("testClientRole", "testClient"))
        ).executeIt()

        val user = client.userByName(testRealm, "test")
        assertThat(client.userGroups(testRealm, user.id).map { it.name }).containsOnly("testGroup")
        assertThat(client.userRoles(testRealm, user.id).map { it.name }).contains("testRealmRole")
        assertThat(
            client.userClientRoles(testRealm, user.id, client.clientUUID("testClient", testRealm))
                .map { it.name }).containsOnly("testClientRole")
    }

    @Test
    fun testAddUser_withEmail() {
        val email = "test@example.com"
        AddUserAction(testRealm, "test", email = email).executeIt()

        val user = client.userByName(testRealm, "test")
        assertThat(user.email).isEqualTo(email)
    }
}
