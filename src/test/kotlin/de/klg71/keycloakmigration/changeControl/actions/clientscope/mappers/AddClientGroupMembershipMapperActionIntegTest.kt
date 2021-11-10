package de.klg71.keycloakmigration.changeControl.actions.clientscope.mappers

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.clientscope.AddClientScopeAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeGroupMembershipMapperAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddClientScopeGroupMembershipMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-group-membership-mapper"
    val claimName = "claimName"

    val clientScopeName = "simpleClientScope"

    @Test
    fun testAddMapper() {
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddClientScopeGroupMembershipMapperAction(
            testRealm, clientScopeName, mapperName,
            claimName = claimName
        ).executeIt()

        val mappers = client.clientScopeMappers(client.clientScopeUUID(clientScopeName, testRealm), testRealm)

        assertThat(mappers).hasSize(1)

        val mapper = mappers[0]

        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
        assertThat(mapper.config["claim.name"]).isEqualTo(claimName)
    }

    @Test
    fun testAddExistingMapper() {
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddClientScopeGroupMembershipMapperAction(
            testRealm, clientScopeName, mapperName,
            claimName = claimName
        ).executeIt()

        assertThatThrownBy {
            AddClientScopeGroupMembershipMapperAction(
                testRealm, clientScopeName, mapperName,
                claimName = claimName
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Mapper with name: $mapperName already exists in client scope: $clientScopeName on realm: $testRealm!")

    }
}
