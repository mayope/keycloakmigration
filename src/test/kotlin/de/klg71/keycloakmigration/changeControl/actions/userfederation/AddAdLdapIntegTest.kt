package de.klg71.keycloakmigration.changeControl.actions.userfederation

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddAdLdapIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddAdLdap() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        AddAdLdapAction(testRealm, "test", ldapConfig).executeIt()

        val createdFederation = client.userFederationByName("test", testRealm);

        assertThat(createdFederation.name).isEqualTo("test")
        assertThat(createdFederation.config["connectionUrl"]).containsOnly(ldapConfig["connectionUrl"])
        assertThat(createdFederation.config["usersDn"]).containsOnly(ldapConfig["usersDn"])
        assertThat(createdFederation.config["bindCredential"]).hasSize(1)
        assertThat(createdFederation.config["bindDn"]).containsOnly(ldapConfig["bindDn"])
    }

    @Test
    fun testAddAdLdapAlreadyExists() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        AddAdLdapAction(testRealm, "test", ldapConfig).executeIt()

        assertThatThrownBy {
            AddAdLdapAction(testRealm, "test", ldapConfig).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("UserFederation with name: test already exists in realm: ${testRealm}!")

    }
}
