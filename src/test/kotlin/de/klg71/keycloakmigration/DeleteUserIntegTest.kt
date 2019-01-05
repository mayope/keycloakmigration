package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.changeControl.actions.user.AddUserAction
import de.klg71.keycloakmigration.changeControl.actions.user.DeleteUserAction
import de.klg71.keycloakmigration.model.UserListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.standalone.inject
import java.util.*

class DeleteUserIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteUser() {
        AddUserAction("master", "test").executeIt()

        DeleteUserAction("master", "test").executeIt()

        UserListItem(UUID.randomUUID(), 0L, "test", true, true).let {
            assertThat(client.users("master")).hasSize(1).usingElementComparatorOnFields("username", "enabled", "emailVerified").doesNotContain(it)
        }

    }
}