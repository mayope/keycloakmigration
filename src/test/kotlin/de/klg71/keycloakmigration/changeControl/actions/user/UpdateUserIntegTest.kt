package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.TEST_BASE_URL
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.initKeycloakLoginClient
import de.klg71.keycloakmigration.keycloakapi.model.UserCredential
import de.klg71.keycloakmigration.keycloakapi.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class UpdateUserIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testUpdateUser() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        UpdateUserAction(testRealm, "testIntegration", email = "testemail").executeIt()

        client.userByName("testIntegration", testRealm).let {
            assertThat(it.email).isEqualTo("testemail")
        }

    }

    @Test
    fun testUpdateUser_userDoesNotExist() {
        assertThatThrownBy {
            UpdateUserAction(testRealm, "testIntegration", email = "testEmail").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("User with name: testIntegration does not exist in realm: ${testRealm}!")

    }

    @Test
    fun testUpdateUser_password() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        val hashedSaltedValue = "1tWf95Drz6t8/9kKE3tiJXOywCzG/C0KDnmCIFXEDdFQMPB6iVWWxjLO9HJI3YwTfWZa78N+hcmYHcT1tkavcA=="
        val salt = "dGVzdA=="
        val password = "1234"

        UpdateUserAction(testRealm, "testIntegration", credentials = listOf(UserCredential(
                salt = salt,
                hashedSaltedValue = hashedSaltedValue)))
                .executeIt()
        val loginClient = initKeycloakLoginClient(TEST_BASE_URL)
        val answer = loginClient.login(testRealm, "password", "admin-cli", "testIntegration", password,"")
        assertThat(answer.accessToken).isNotEmpty()
    }
}
