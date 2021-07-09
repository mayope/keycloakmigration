package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.AddClientScopeAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject

class AddUserRealmRoleMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-usermodel-realm-role-mapper"
    val claimName = "claimName"

    val clientId = "simpleClient"
    val clientScopeName = "simpleClientScope"

    @Test
    fun testAddMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddUserRealmRoleMapperAction(
            testRealm, mapperName, clientId, clientScopeName
        ).executeIt()

        val clientMappers = client.clientMappers(client.clientUUID(clientId, testRealm), testRealm)

        assertThat(clientMappers).hasSize(1)

        val clientMapper = clientMappers[0]

        assertThat(clientMapper.name).isEqualTo(mapperName)
        assertThat(clientMapper.protocol).isEqualTo(protocol)
        assertThat(clientMapper.protocolMapper).isEqualTo(protocolMapper)

        val mappers = client.mappers(client.clientScopeUUID(clientScopeName, testRealm), testRealm)

        assertThat(mappers).hasSize(1)

        val mapper = mappers[0]

        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
    }

    @Test
    fun testAddMapperWithClaimName() {
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddUserRealmRoleMapperAction(
            testRealm, mapperName, clientId, clientScopeName,
            claimName = claimName
        ).executeIt()

        val clientMappers = client.clientMappers(client.clientUUID(clientId, testRealm), testRealm)
        val clientMapper = clientMappers[0]

        assertThat(clientMapper.name).isEqualTo(mapperName)
        assertThat(clientMapper.config["claim.name"]).isEqualTo(claimName)

        val mappers = client.mappers(client.clientScopeUUID(clientScopeName, testRealm), testRealm)
        val mapper = mappers[0]

        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.config["claim.name"]).isEqualTo(claimName)
    }
}
