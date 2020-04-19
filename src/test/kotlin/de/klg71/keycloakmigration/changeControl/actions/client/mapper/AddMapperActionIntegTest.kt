package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class AddMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddMapper() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val config = mapOf(
                "access.token.claim" to "true",
                "claim.name" to "email",
                "id.token.claim" to "true",
                "jsonType.label" to "String",
                "user.attribute" to "UserModel.getEmail()",
                "userinfo.token.claim" to "true")
        val mapperName = "testMapper"
        val protocol = "openid-connect"
        val protocolMapper = "oidc-usermodel-property-mapper"
        AddMapperAction(testRealm, "simpleClient", mapperName, config,
                protocolMapper, protocol).executeIt()

        val mappers = client.mappers(client.clientUUID("simpleClient", testRealm), testRealm)

        assertThat(mappers).hasSize(1)
        val mapper = mappers[0]
        assertThat(mapper.config).isEqualTo(config)
        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
    }

    @Test
    fun testAddExistingMapper() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val config = mapOf(
                "access.token.claim" to "true",
                "claim.name" to "email",
                "id.token.claim" to "true",
                "jsonType.label" to "String",
                "user.attribute" to "UserModell.getEmail()",
                "userinfo.token.claim" to "true")
        val mapperName = "testMapper"
        val protocol = "openid-connect"
        val protocolMapper = "oidc-usermodel-property-mapper"
        AddMapperAction(testRealm, "simpleClient", mapperName, config,
                protocolMapper, protocol).executeIt()
        assertThatThrownBy {
            AddMapperAction(testRealm, "simpleClient", mapperName, config,
                    protocolMapper, protocol).executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Mapper with name: $mapperName already exists in client: simpleClient on realm: $testRealm!")

    }
}
