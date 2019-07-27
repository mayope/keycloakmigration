package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.UserCredential
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.KeycloakLoginClient
import de.klg71.keycloakmigration.rest.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class UpdateUserIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val loginClient by inject<KeycloakLoginClient>()

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
        }.isInstanceOf(MigrationException::class.java).hasMessage("User with name: testIntegration does not exist in realm: ${testRealm}!")


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
        val answer = loginClient.login(testRealm, "password", "admin-cli", "testIntegration", password)
        assertThat(answer.accessToken).isNotEmpty()
    }
}