package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.userfederation.AddAdLdapAction
import de.klg71.keycloakmigration.keycloakapi.model.translateConfig
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.ldapMapperByName
import de.klg71.keycloakmigration.keycloakapi.ldapMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class AddAdLdapMapperIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddAdLdapMapper() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        val userFederationName = "testName"
        AddAdLdapAction(testRealm,
                userFederationName, ldapConfig).executeIt()

        val createdFederation = client.userFederationByName(userFederationName, testRealm);

        val config = mapOf(
                "memberof.ldap.attribute" to "memberOf",
                "membership.attribute.type" to "DN",
                "membership.ldap.attribute" to "member",
                "membership.user.ldap.attribute" to "cn",
                "mode" to "READ_ONLY",
                "role.name.ldap.attribute" to "cn",
                "role.object.classes" to "group",
                "roles.dn" to "rolesDn",
                "use.realm.roles.mapping" to "true",
                "user.roles.retrieve.strategy" to "LOAD_ROLES_BY_MEMBERSHIP_ATTRIBUTE"
        )
        val mapperName = "mapperName"
        AddAdLdapMapperAction(
                testRealm, mapperName, createdFederation.name, "role-ldap-mapper", config).executeIt()

        val createdMapper = client.ldapMapperByName(createdFederation.name, mapperName, testRealm)

        assertThat(createdMapper.config).isEqualTo(translateConfig(config))
    }

    @Test
    fun testAddAdLdapMapper_duplicate() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        val userFederationName = "testName"
        AddAdLdapAction(testRealm,
                userFederationName, ldapConfig).executeIt()

        val createdFederation = client.userFederationByName(userFederationName, testRealm);

        val config = mapOf(
                "memberof.ldap.attribute" to "memberOf",
                "membership.attribute.type" to "DN",
                "membership.ldap.attribute" to "member",
                "membership.user.ldap.attribute" to "cn",
                "mode" to "READ_ONLY",
                "role.name.ldap.attribute" to "cn",
                "role.object.classes" to "group",
                "roles.dn" to "rolesDn",
                "use.realm.roles.mapping" to "true",
                "user.roles.retrieve.strategy" to "LOAD_ROLES_BY_MEMBERSHIP_ATTRIBUTE"
        )
        val mapperName = "mapperName"
        AddAdLdapMapperAction(
                testRealm, mapperName, createdFederation.name, "role-ldap-mapper", config).executeIt()
        assertThatThrownBy {
            AddAdLdapMapperAction(
                    testRealm, mapperName, createdFederation.name, "role-ldap-mapper", config).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage(
                        "Mapper with name: $mapperName already exists in UserFederation with name: ${createdFederation.name} in realm: $testRealm!")

    }

    @Test
    fun testAddAdLdapMapper_Undo() {
        val ldapConfig = mapOf(
                "connectionUrl" to "https://testUrl",
                "usersDn" to "usersTestDn",
                "bindCredential" to "testPassword",
                "bindDn" to "testBindDn")
        val userFederationName = "testName"
        AddAdLdapAction(testRealm,
                userFederationName, ldapConfig).executeIt()

        val createdFederation = client.userFederationByName(userFederationName, testRealm);

        val config = mapOf(
                "memberof.ldap.attribute" to "memberOf",
                "membership.attribute.type" to "DN",
                "membership.ldap.attribute" to "member",
                "membership.user.ldap.attribute" to "cn",
                "mode" to "READ_ONLY",
                "role.name.ldap.attribute" to "cn",
                "role.object.classes" to "group",
                "roles.dn" to "rolesDn",
                "use.realm.roles.mapping" to "true",
                "user.roles.retrieve.strategy" to "LOAD_ROLES_BY_MEMBERSHIP_ATTRIBUTE"
        )
        val mapperName = "mapperName"
        val action = AddAdLdapMapperAction(
                testRealm, mapperName, createdFederation.name, "role-ldap-mapper", config)
        action.executeIt()
        action.undoIt()

        assertThat(client.ldapMapperExistsByName(createdFederation.name, mapperName, testRealm)).isFalse()
    }

}
