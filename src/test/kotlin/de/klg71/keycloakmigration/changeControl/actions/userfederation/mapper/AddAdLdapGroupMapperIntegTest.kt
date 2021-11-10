package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.AddAdLdapAction
import de.klg71.keycloakmigration.keycloakapi.model.GROUP_MAPPER
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.ldapMapperByName
import de.klg71.keycloakmigration.keycloakapi.ldapMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class AddAdLdapGroupMapperIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddAdLdapGroupMapper() {
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
        val groupsDns = "groupsDns"
        AddAdLdapGroupMapperAction(
                testRealm, mapperName, createdFederation.name, groupsDns).executeIt()

        val createdMapper = client.ldapMapperByName(createdFederation.name, mapperName, testRealm)

        assertThat((createdMapper.config["groups.dn"] ?: error("test error"))[0]).isEqualTo(groupsDns)
        assertThat(createdMapper.providerId).isEqualTo(GROUP_MAPPER)
    }


    @Test
    fun testAddAdLdapGroupMapper_Undo() {
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
        val groupsDns = "groupsDns"
        val action = AddAdLdapGroupMapperAction(
                testRealm, mapperName, createdFederation.name, groupsDns)
        action.executeIt()
        action.undoIt()

        assertThat(client.ldapMapperExistsByName(createdFederation.name, mapperName, testRealm)).isFalse()
    }
}
