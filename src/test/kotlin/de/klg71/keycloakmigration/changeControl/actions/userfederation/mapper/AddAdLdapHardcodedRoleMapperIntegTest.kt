package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.AddAdLdapAction
import de.klg71.keycloakmigration.keycloakapi.model.HARDCODED_LDAP_ROLE_MAPPER
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.ldapMapperByName
import de.klg71.keycloakmigration.keycloakapi.ldapMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class AddAdLdapHardcodedRoleMapperIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddAdLdapHardcodedRoleMapper() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        val userFederationName = "testName"
        AddAdLdapAction(testRealm,
                userFederationName, ldapConfig).executeIt()

        val createdFederation = client.userFederationByName(userFederationName, testRealm);

        val role = "role"
        AddRoleAction(testRealm, role).executeIt()

        val mapperName = "mapperName"
        AddAdLdapHardcodedRoleMapperAction(
                testRealm, mapperName, createdFederation.name, role).executeIt()

        val createdMapper = client.ldapMapperByName(createdFederation.name, mapperName, testRealm)

        assertThat((createdMapper.config["role"] ?: error("test error"))[0]).isEqualTo(role)
        assertThat(createdMapper.providerId).isEqualTo(HARDCODED_LDAP_ROLE_MAPPER)
    }

    @Test
    fun testAddAdLdapHardcodedRoleMapper_clientRole() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        val userFederationName = "testName"
        AddAdLdapAction(testRealm,
                userFederationName, ldapConfig).executeIt()

        val createdFederation = client.userFederationByName(userFederationName, testRealm);

        AddSimpleClientAction(testRealm, "testClient").executeIt()
        val role = "role"
        AddRoleAction(testRealm, role, "testClient").executeIt()

        val mapperName = "mapperName"
        assertThatThrownBy {
            AddAdLdapHardcodedRoleMapperAction(
                    testRealm, mapperName, createdFederation.name, role).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Realm role with name: $role does not exist in realm: $testRealm!")
    }

    @Test
    fun testAddAdLdapHardcodedRoleMapper_noRoleFound() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        val userFederationName = "testName"
        AddAdLdapAction(testRealm,
                userFederationName, ldapConfig).executeIt()

        val createdFederation = client.userFederationByName(userFederationName, testRealm);

        val mapperName = "mapperName"
        val role = "role"
        assertThatThrownBy {
            AddAdLdapHardcodedRoleMapperAction(
                    testRealm, mapperName, createdFederation.name, role).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Realm role with name: $role does not exist in realm: $testRealm!")
    }

    @Test
    fun testAddAdLdapHardcodedRoleMapper_Undo() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        val userFederationName = "testName"
        AddAdLdapAction(testRealm,
                userFederationName, ldapConfig).executeIt()

        val createdFederation = client.userFederationByName(userFederationName, testRealm);

        val role = "role"
        AddRoleAction(testRealm, role).executeIt()
        val mapperName = "mapperName"
        val action = AddAdLdapHardcodedRoleMapperAction(
                testRealm, mapperName, createdFederation.name, role)
        action.executeIt()
        action.undoIt()

        assertThat(client.ldapMapperExistsByName(createdFederation.name, mapperName, testRealm)).isFalse()
    }
}
