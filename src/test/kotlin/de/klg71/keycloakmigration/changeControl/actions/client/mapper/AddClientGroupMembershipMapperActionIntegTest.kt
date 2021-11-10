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

class AddClientGroupMembershipMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-group-membership-mapper"
    val claimName = "claimName"

    val clientId = "simpleClient"

    @Test
    fun testAddMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        AddClientGroupMembershipMapperAction(
            testRealm, clientId, mapperName,
            claimName = claimName
        ).executeIt()

        val clientMappers = client.clientMappers(client.clientUUID(clientId, testRealm), testRealm)

        assertThat(clientMappers).hasSize(1)

        val clientMapper = clientMappers[0]

        assertThat(clientMapper.name).isEqualTo(mapperName)
        assertThat(clientMapper.protocol).isEqualTo(protocol)
        assertThat(clientMapper.protocolMapper).isEqualTo(protocolMapper)
        assertThat(clientMapper.config["claim.name"]).isEqualTo(claimName)
    }

    @Test
    fun testAddExistingMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        AddClientGroupMembershipMapperAction(
            testRealm, clientId, mapperName,
            claimName = claimName
        ).executeIt()

        assertThatThrownBy {
            AddClientGroupMembershipMapperAction(
                testRealm, clientId, mapperName,
                claimName = claimName
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Mapper with name: $mapperName already exists in client: $clientId on realm: $testRealm!")

    }
}
