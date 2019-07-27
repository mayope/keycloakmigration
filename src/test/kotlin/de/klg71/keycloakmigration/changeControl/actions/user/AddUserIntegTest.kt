package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.model.UserListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject
import java.util.*

class AddUserIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddUser() {
        AddUserAction(testRealm, "test").executeIt()

        UserListItem(UUID.randomUUID(), 0L, "test", true, true).let {
            assertThat(client.users(testRealm)).usingElementComparatorOnFields("username", "enabled", "emailVerified").contains(it)
        }

    }
}