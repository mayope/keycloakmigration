package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.userfederation.AddAdLdapAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.ldapMapperByName
import de.klg71.keycloakmigration.keycloakapi.ldapMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.model.USER_ATTRIBUTE_MAPPER
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class AddAdLdapUserAttributeMapperIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddAdLdapUserAttributeMapper() {
        val ldapConfig = mapOf(
            "connectionUrl" to "https://testUrl",
            "usersDn" to "usersTestDn",
            "bindCredential" to "testPassword",
            "bindDn" to "testBindDn"
        )
        val userFederationName = "testName"
        AddAdLdapAction(
            testRealm,
            userFederationName, ldapConfig
        ).executeIt()

        val createdFederation = client.userFederationByName(userFederationName, testRealm);

        val mapperName = "mapperName"
        val userModelAttribute = "userModelAttribute"
        val ldapAttribute = "ldapAttribute"
        AddAdLdapUserAttributeMapperAction(
            testRealm, mapperName, createdFederation.name, userModelAttribute, ldapAttribute
        ).executeIt()

        val createdMapper = client.ldapMapperByName(createdFederation.name, mapperName, testRealm)

        assertThat((createdMapper.config["user.model.attribute"] ?: error("test error"))[0]).isEqualTo(
            userModelAttribute
        )
        assertThat((createdMapper.config["ldap.attribute"] ?: error("test error"))[0]).isEqualTo(ldapAttribute)
        assertThat((createdMapper.config["read.only"] ?: error("test error"))[0]).isEqualTo("false")
        assertThat((createdMapper.config["always.read.value.from.ldap"] ?: error("test error"))[0]).isEqualTo("false")
        assertThat((createdMapper.config["is.mandatory.in.ldap"] ?: error("test error"))[0]).isEqualTo("false")
        assertThat(createdMapper.providerId).isEqualTo(USER_ATTRIBUTE_MAPPER)
    }

    @Test
    fun testAddAdLdapUserAttributeMapper_Undo() {
        val ldapConfig = mapOf(
            "connectionUrl" to "https://testUrl",
            "usersDn" to "usersTestDn",
            "bindCredential" to "testPassword",
            "bindDn" to "testBindDn"
        )
        val userFederationName = "testName"
        AddAdLdapAction(
            testRealm,
            userFederationName, ldapConfig
        ).executeIt()

        val createdFederation = client.userFederationByName(userFederationName, testRealm);

        val mapperName = "mapperName"
        val userModelAttribute = "userModelAttribute"
        val ldapAttribute = "ldapAttribute"

        val action = AddAdLdapUserAttributeMapperAction(
            testRealm, mapperName, createdFederation.name, userModelAttribute, ldapAttribute
        )
        action.executeIt()
        action.undoIt()

        assertThat(client.ldapMapperExistsByName(createdFederation.name, mapperName, testRealm)).isFalse()
    }
}
