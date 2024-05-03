package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddClientMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val config = mapOf(
        "access.token.claim" to "true",
        "claim.name" to "email",
        "id.token.claim" to "true",
        "jsonType.label" to "String",
        "user.attribute" to "UserModel.getEmail()",
        "introspection.token.claim" to "true",
        "userinfo.token.claim" to "true"
    )
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-usermodel-property-mapper"

    val clientId = "simpleClient"

    @Test
    fun testAddMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        AddClientMapperAction(
            testRealm, clientId, mapperName,
            config, protocol, protocolMapper
        ).executeIt()

        val clientMappers = client.clientMappers(client.clientUUID(clientId, testRealm), testRealm)

        assertThat(clientMappers).hasSize(1)

        val clientMapper = clientMappers[0]

        assertThat(clientMapper.config).isEqualTo(config)
        assertThat(clientMapper.name).isEqualTo(mapperName)
        assertThat(clientMapper.protocol).isEqualTo(protocol)
        assertThat(clientMapper.protocolMapper).isEqualTo(protocolMapper)
    }

    @Test
    fun testAddExistingMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt();

        AddClientMapperAction(
            testRealm, clientId, mapperName,
            config, protocol, protocolMapper
        ).executeIt()

        assertThatThrownBy {
            AddClientMapperAction(
                testRealm, clientId, mapperName,
                config, protocol, protocolMapper
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Mapper with name: $mapperName already exists in client: $clientId on realm: $testRealm!")

    }

}
