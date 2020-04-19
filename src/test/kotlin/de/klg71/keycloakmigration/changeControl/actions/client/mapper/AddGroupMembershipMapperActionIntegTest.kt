package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject

class AddGroupMembershipMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddMapper() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val mapperName = "testMapper"
        val protocol = "openid-connect"
        val protocolMapper = "oidc-group-membership-mapper"
        AddGroupMembershipMapperAction(testRealm, "simpleClient", mapperName).executeIt()

        val mappers = client.mappers(client.clientUUID("simpleClient", testRealm), testRealm)

        assertThat(mappers).hasSize(1)
        val mapper = mappers[0]
        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
        assertThat(mapper.config["claim.name"]).isEqualTo(mapperName)
    }

    @Test
    fun testAddMapper_claimNameGiven() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val mapperName = "testMapper"
        val claimName = "claimName"
        AddGroupMembershipMapperAction(testRealm, "simpleClient", mapperName, claimName = claimName).executeIt()

        val mappers = client.mappers(client.clientUUID("simpleClient", testRealm), testRealm)

        assertThat(mappers).hasSize(1)
        val mapper = mappers[0]
        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.config["claim.name"]).isEqualTo(claimName)
    }

    @Test
    fun testAddMapper_fullPathFalse() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val mapperName = "testMapper"
        AddGroupMembershipMapperAction(testRealm, "simpleClient", mapperName, fullGroupPath = false).executeIt()

        val mappers = client.mappers(client.clientUUID("simpleClient", testRealm), testRealm)

        assertThat(mappers).hasSize(1)
        val mapper = mappers[0]
        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.config["full.path"]).isEqualTo("false")
    }
}
