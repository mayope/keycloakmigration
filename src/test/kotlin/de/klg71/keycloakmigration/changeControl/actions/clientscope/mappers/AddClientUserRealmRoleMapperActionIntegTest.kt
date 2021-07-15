package de.klg71.keycloakmigration.changeControl.actions.clientscope.mappers

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.clientscope.AddClientScopeAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeUserRealmRoleMapperAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject

class AddClientScopeUserRealmRoleMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-usermodel-realm-role-mapper"
    val claimName = "claimName"

    val clientScopeName = "simpleClientScope"

    @Test
    fun testAddMapper() {
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddClientScopeUserRealmRoleMapperAction(testRealm, clientScopeName, mapperName).executeIt()

        val mappers = client.clientScopeMappers(client.clientScopeUUID(clientScopeName, testRealm), testRealm)

        assertThat(mappers).hasSize(1)

        val mapper = mappers[0]

        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
    }

    @Test
    fun testAddMapperWithClaimName() {
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddClientScopeUserRealmRoleMapperAction(
            testRealm, clientScopeName, mapperName,
            claimName = claimName
        ).executeIt()

        val mappers = client.clientScopeMappers(client.clientScopeUUID(clientScopeName, testRealm), testRealm)
        val mapper = mappers[0]

        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.config["claim.name"]).isEqualTo(claimName)
    }
}
