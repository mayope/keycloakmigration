package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject
import java.util.UUID

class DeleteUserIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteUser() {
        AddUserAction(testRealm, "test").executeIt()

        DeleteUserAction(testRealm, "test").executeIt()

        User(
            id = UUID.randomUUID(), createdTimestamp = 0L, username = "test", enabled = true, emailVerified = true,
            notBefore = 1L, totp = false, requiredActions = emptyList(), attributes = null, access = null,
            disableableCredentialTypes = emptyList(), email = null, firstName = null, lastName = null,
            credentials = emptyList()
        ).let {
            assertThat(client.users(testRealm)).usingElementComparatorOnFields("username", "enabled", "emailVerified")
                .doesNotContain(it)
        }

    }
}
