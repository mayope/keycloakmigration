package de.klg71.keycloakmigration.changeControl.actions.clientscope.mappers

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.clientscope.AddClientScopeAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeAudienceMapperAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddClientScopeAudienceMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-audience-mapper"
    val clientAudience = "clientAudience"
    val customAudience = "customAudience"

    val clientScopeName = "simpleClientScope"

    @Test
    fun testAddMapper() {
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddClientScopeAudienceMapperAction(
            testRealm, clientScopeName, mapperName,
            clientAudience = clientAudience,
            customAudience = customAudience
        ).executeIt()

        val mappers = client.clientScopeMappers(client.clientScopeUUID(clientScopeName, testRealm), testRealm)

        assertThat(mappers).hasSize(1)

        val mapper = mappers[0]

        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
        assertThat(mapper.config["included.client.audience"]).isEqualTo(clientAudience)
        assertThat(mapper.config["included.custom.audience"]).isEqualTo(customAudience)
    }

    @Test
    fun testAddExistingMapper() {
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddClientScopeAudienceMapperAction(
            testRealm, clientScopeName, mapperName,
            clientAudience = clientAudience,
            customAudience = customAudience
        ).executeIt()

        assertThatThrownBy {
            AddClientScopeAudienceMapperAction(
                testRealm, clientScopeName, mapperName,
                clientAudience = clientAudience,
                customAudience = customAudience
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage(
                "Mapper with name: $mapperName already exists in client scope: $clientScopeName on realm: $testRealm!"
            )
    }
}
