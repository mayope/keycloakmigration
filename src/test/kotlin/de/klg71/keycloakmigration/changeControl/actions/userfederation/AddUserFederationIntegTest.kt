package de.klg71.keycloakmigration.changeControl.actions.userfederation

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.constructAdLdapConfig
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import feign.FeignException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class AddUserFederationIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddUserFederation() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        AddUserFederationAction(
                testRealm, "test",
                constructAdLdapConfig(ldapConfig)
                        .filter { (_, value) -> value.isNotEmpty() }
                        .mapValues { it.value.first() },
                "ldap", "org.keycloak.storage.UserStorageProvider"
        ).executeIt()

        val createdFederation = client.userFederationByName("test", testRealm);

        assertThat(createdFederation.name).isEqualTo("test")
        assertThat(createdFederation.config["connectionUrl"]).containsOnly(ldapConfig["connectionUrl"])
        assertThat(createdFederation.config["usersDn"]).containsOnly(ldapConfig["usersDn"])
        assertThat(createdFederation.config["bindCredential"]).hasSize(1)
        assertThat(createdFederation.config["bindDn"]).containsOnly(ldapConfig["bindDn"])
    }

    @Test
    fun testAddUserFederationAlreadyExists() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        AddUserFederationAction(
                testRealm, "test",
                constructAdLdapConfig(ldapConfig)
                        .filter { (_, value) -> value.isNotEmpty() }
                        .mapValues { it.value.first() },
                "ldap", "org.keycloak.storage.UserStorageProvider"
        ).executeIt()

        assertThatThrownBy {
            AddUserFederationAction(
                    testRealm, "test",
                    constructAdLdapConfig(ldapConfig)
                            .filter { (_, value) -> value.isNotEmpty() }
                            .mapValues { it.value.first() },
                    "ldap", "org.keycloak.storage.UserStorageProvider"
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("UserFederation with name: test already exists in realm: ${testRealm}!")
    }

    @Test
    fun testAddUserFederation_Rollback() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        val action = AddUserFederationAction(
                testRealm, "test",
                constructAdLdapConfig(ldapConfig)
                        .filter { (_, value) -> value.isNotEmpty() }
                        .mapValues { it.value.first() },
                "ldap", "org.keycloak.storage.UserStorageProvider"
        )

        action.executeIt()
        val createdFederation = client.userFederationByName("test", testRealm)
        assertThat(createdFederation.name).isEqualTo("test")

        action.undoIt()
        assertThatThrownBy {
            client.userFederationByName("test", testRealm)
        }.isInstanceOf(KeycloakApiException::class.java).hasMessageContaining("UserFederation with name: test does not exist in test!")
    }
}
