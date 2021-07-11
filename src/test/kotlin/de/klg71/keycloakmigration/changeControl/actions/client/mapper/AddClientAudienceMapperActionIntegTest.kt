package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.AddClientScopeAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class AddClientAudienceMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-audience-mapper"
    val clientAudience = "clientAudience"
    val customAudience = "customAudience"

    val clientId = "simpleClient"

    @Test
    fun testAddMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        AddClientAudienceMapperAction(
            testRealm, mapperName, clientId,
            clientAudience = clientAudience,
            customAudience = customAudience
        ).executeIt()

        val clientMappers = client.clientMappers(client.clientUUID(clientId, testRealm), testRealm)

        assertThat(clientMappers).hasSize(1)

        val clientMapper = clientMappers[0]

        assertThat(clientMapper.name).isEqualTo(mapperName)
        assertThat(clientMapper.protocol).isEqualTo(protocol)
        assertThat(clientMapper.protocolMapper).isEqualTo(protocolMapper)
        assertThat(clientMapper.config["included.client.audience"]).isEqualTo(clientAudience)
        assertThat(clientMapper.config["included.custom.audience"]).isEqualTo(customAudience)
    }

    @Test
    fun testAddExistingMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        AddClientAudienceMapperAction(
            testRealm, mapperName, clientId,
            clientAudience = clientAudience,
            customAudience = customAudience
        ).executeIt()

        assertThatThrownBy {
            AddClientAudienceMapperAction(
                testRealm, mapperName, clientId,
                clientAudience = clientAudience,
                customAudience = customAudience
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Mapper with name: $mapperName already exists in client: $clientId on realm: $testRealm!")
    }
}
