package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.KeycloakLoginClient
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class UpdateUserPasswordIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val loginClient by inject<KeycloakLoginClient>()

    @Test
    fun testUpdateUserPassword() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        val password = RandomStringUtils.randomAlphanumeric(15)
        UpdateUserPasswordAction(testRealm, "testIntegration", password = password).executeIt()

        val answer = loginClient.login(testRealm, "password", "admin-cli", "testIntegration", password)
        assertThat(answer.accessToken).isNotEmpty()
    }

    @Test
    fun testUpdateUserPasswordWithSalt() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        val password = RandomStringUtils.randomAlphanumeric(15)
        val salt = RandomStringUtils.randomAlphanumeric(15)
        UpdateUserPasswordAction(testRealm, "testIntegration", password = password, salt = salt).executeIt()

        val answer = loginClient.login(testRealm, "password", "admin-cli", "testIntegration", password)
        assertThat(answer.accessToken).isNotEmpty()
    }

    @Test
    fun testUpdateUser_userDoesNotExist() {
        assertThatThrownBy {
            UpdateUserPasswordAction(testRealm, "testIntegration", password = "testPassword").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("User with name: testIntegration does not exist in realm: ${testRealm}!")


    }
}
