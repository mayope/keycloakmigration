package de.klg71.keycloakmigration.changeControl.actions.userfederation

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class DeleteUserFederationIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteUserFederation() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        AddAdLdapAction(testRealm, "test", ldapConfig).executeIt()
        DeleteUserFederationAction(testRealm,"test").executeIt()

        assertThat(client.userFederations(testRealm)).isEmpty();
    }

    @Test
    fun testDeleteUserFederationNotExisting() {
        assertThatThrownBy {
            DeleteUserFederationAction(testRealm,"test").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("UserFederation with name: test does not exist in realm: ${testRealm}!")

    }
}
