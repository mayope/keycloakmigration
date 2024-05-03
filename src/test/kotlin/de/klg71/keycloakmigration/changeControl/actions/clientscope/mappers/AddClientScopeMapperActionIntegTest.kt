package de.klg71.keycloakmigration.changeControl.actions.clientscope.mappers

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.clientscope.AddClientScopeAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeMapperAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddClientScopeMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val config = mapOf(
        "userinfo.token.claim" to "true",
        "user.attribute" to "title",
        "id.token.claim" to "false",
        "introspection.token.claim" to "true",
        "access.token.claim" to "true",
        "claim.name" to "title",
        "jsonType.label" to "String"
    )
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-usermodel-attribute-mapper"

    val clientScopeName = "simpleClientScope"

    @Test
    fun testAddMapper() {
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddClientScopeMapperAction(
            testRealm, clientScopeName, mapperName,
            config, protocol, protocolMapper
        ).executeIt()

        val mappers = client.clientScopeMappers(client.clientScopeUUID(clientScopeName, testRealm), testRealm)

        assertThat(mappers).hasSize(1)

        val mapper = mappers[0]

        assertThat(mapper.config).isEqualTo(config)
        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
    }

    @Test
    fun testAddExistingMapper() {
        AddClientScopeAction(testRealm, clientScopeName).executeIt();

        AddClientScopeMapperAction(
            testRealm, clientScopeName, mapperName,
            config, protocol, protocolMapper
        ).executeIt()

        assertThatThrownBy {
            AddClientScopeMapperAction(
                testRealm, clientScopeName, mapperName,
                config, protocol, protocolMapper
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage(
                "Mapper with name: $mapperName already exists in client scope: $clientScopeName" +
                        " on realm: $testRealm!"
            )

    }

}
