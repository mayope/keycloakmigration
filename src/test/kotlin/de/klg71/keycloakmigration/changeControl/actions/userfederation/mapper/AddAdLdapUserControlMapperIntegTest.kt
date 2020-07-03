package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.userfederation.AddAdLdapAction
import de.klg71.keycloakmigration.keycloakapi.model.USER_ACCOUNT_CONTROL_MAPPER
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.ldapMapperByName
import de.klg71.keycloakmigration.keycloakapi.ldapMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject

class AddAdLdapUserControlMapperIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddAdLdapUserControlMapper() {
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
        AddAdLdapUserAccountControlMapperAction(
                testRealm, mapperName, createdFederation.name).executeIt()

        val createdMapper = client.ldapMapperByName(createdFederation.name, mapperName, testRealm)

        assertThat(createdMapper.providerId).isEqualTo(USER_ACCOUNT_CONTROL_MAPPER)
    }


    @Test
    fun testAddAdLdapUserControlMapper_Undo() {
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
        val action = AddAdLdapUserAccountControlMapperAction(
                testRealm, mapperName, createdFederation.name)
        action.executeIt()
        action.undoIt()

        assertThat(client.ldapMapperExistsByName(createdFederation.name, mapperName, testRealm)).isFalse()
    }
}
