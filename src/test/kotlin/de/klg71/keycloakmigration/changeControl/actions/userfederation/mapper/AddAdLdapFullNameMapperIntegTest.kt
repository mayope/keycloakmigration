package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.userfederation.AddAdLdapAction
import de.klg71.keycloakmigration.model.FULL_NAME_MAPPER
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.ldapMapperByName
import de.klg71.keycloakmigration.rest.ldapMapperExistsByName
import de.klg71.keycloakmigration.rest.userFederationByName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject

class AddAdLdapFullNameMapperIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddAdLdapFullNameMapper() {
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
        val ldapFullNameAttribute = "fullNameAttribute"
        AddAdLdapFullNameMapperAction(
                testRealm, mapperName, createdFederation.name, ldapFullNameAttribute).executeIt()

        val createdMapper = client.ldapMapperByName(createdFederation.name, mapperName, testRealm)

        assertThat((createdMapper.config["ldap.full.name.attribute"] ?: error("test error"))[0]).isEqualTo(ldapFullNameAttribute)
        assertThat(createdMapper.providerId).isEqualTo(FULL_NAME_MAPPER)
    }

    @Test
    fun testAddAdLdapFullNameMapper_Undo() {
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
        val ldapFullNameAttribute = "fullNameAttribute"
        val action = AddAdLdapFullNameMapperAction(
                testRealm, mapperName, createdFederation.name, ldapFullNameAttribute)
        action.executeIt()
        action.undoIt()

        assertThat(client.ldapMapperExistsByName(createdFederation.name, mapperName, testRealm)).isFalse()
    }
}
