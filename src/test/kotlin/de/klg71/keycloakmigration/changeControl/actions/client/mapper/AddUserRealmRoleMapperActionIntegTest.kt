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

class AddUserRealmRoleMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddMapper() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val mapperName = "testMapper"
        val protocol = "openid-connect"
        val protocolMapper = "oidc-usermodel-realm-role-mapper"
        AddUserRealmRoleMapperAction(testRealm, "simpleClient", mapperName).executeIt()

        val mappers = client.mappers(client.clientUUID("simpleClient", testRealm), testRealm)

        assertThat(mappers).hasSize(1)
        val mapper = mappers[0]
        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
    }

    @Test
    fun testAddMapper_claimNameGiven() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val mapperName = "testMapper"
        val claimName = "claimName"
        AddUserRealmRoleMapperAction(testRealm, "simpleClient", mapperName,claimName = claimName).executeIt()

        val mappers = client.mappers(client.clientUUID("simpleClient", testRealm), testRealm)

        assertThat(mappers).hasSize(1)
        val mapper = mappers[0]
        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.config["claim.name"]).isEqualTo(claimName)
    }
}
