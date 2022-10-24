package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.TEST_BASE_URL
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.initKeycloakLoginClient
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class UpdateUserPasswordIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    private val loginClient = initKeycloakLoginClient(TEST_BASE_URL)

    @Test
    fun testUpdateUserPassword() {
        AddUserAction(testRealm, "testIntegration").executeIt()
        val password = RandomStringUtils.randomAlphanumeric(15)
        UpdateUserPasswordAction(testRealm, "testIntegration", password = password).executeIt()

        val answer = loginClient.login(testRealm, "password", "admin-cli", "testIntegration", password, "")
        assertThat(answer.accessToken).isNotEmpty()
    }

    @Test
    fun testUpdateUserPassword_userDoesNotExist() {
        assertThatThrownBy {
            UpdateUserPasswordAction(testRealm, "testIntegration", password = "testPassword").executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("User with name: testIntegration does not exist in realm: ${testRealm}!")
    }
}
