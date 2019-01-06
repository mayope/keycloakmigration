package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.model.UserListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject
import java.util.*

class AddUserIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddUser() {
        AddUserAction("master", "test").executeIt()

        UserListItem(UUID.randomUUID(), 0L, "test", true, true).let {
            assertThat(client.users("master")).hasSize(2).usingElementComparatorOnFields("username", "enabled", "emailVerified").contains(it)
        }

    }

    @After
    fun cleanup() {
        DeleteUserAction("master", "test").executeIt()
    }
}