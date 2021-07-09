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

class AddGroupMembershipMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-group-membership-mapper"
    val claimName = "claimName"

    val clientId = "simpleClient"
    val clientScopeName = "simpleClientScope"

    @Test
    fun testAddMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddClientScopeAction(testRealm, clientScopeName).executeIt();

        AddGroupMembershipMapperAction(
            testRealm, mapperName, clientId, clientScopeName,
            claimName = claimName
        ).executeIt()

        val clienMappers = client.clientMappers(client.clientUUID(clientId, testRealm), testRealm)

        assertThat(clienMappers).hasSize(1)

        val clientMapper = clienMappers[0]

        assertThat(clientMapper.name).isEqualTo(mapperName)
        assertThat(clientMapper.protocol).isEqualTo(protocol)
        assertThat(clientMapper.protocolMapper).isEqualTo(protocolMapper)
        assertThat(clientMapper.config["claim.name"]).isEqualTo(claimName)

        val mappers = client.mappers(client.clientScopeUUID(clientScopeName, testRealm), testRealm)

        assertThat(mappers).hasSize(1)

        val mapper = mappers[0]

        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
        assertThat(mapper.config["claim.name"]).isEqualTo(claimName)
    }

    @Test
    fun testAddExistingMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddClientScopeAction(testRealm, clientScopeName).executeIt();

        AddGroupMembershipMapperAction(
            testRealm, mapperName, clientId, clientScopeName,
            claimName = claimName
        ).executeIt()

        assertThatThrownBy {
            AddGroupMembershipMapperAction(
                testRealm, mapperName, clientId,
                claimName = claimName
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Mapper with name: $mapperName already exists in client: $clientId on realm: $testRealm!")

        assertThatThrownBy {
            AddGroupMembershipMapperAction(
                testRealm, mapperName, null, clientScopeName,
                claimName = claimName
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Mapper with name: $mapperName already exists in client scope: $clientScopeName on realm: $testRealm!")
    }
}
