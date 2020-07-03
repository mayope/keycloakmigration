package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class AddUserAttributeMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddMapper_claimNameNotGiven() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val mapperName = "testMapper"
        val protocol = "openid-connect"
        val protocolMapper = "oidc-usermodel-attribute-mapper"
        val userAttribute = "testAttribute"
        AddUserAttributeMapperAction(testRealm, "simpleClient", mapperName, userAttribute).executeIt()

        val mappers = client.mappers(client.clientUUID("simpleClient", testRealm), testRealm)

        assertThat(mappers).hasSize(1)
        val mapper = mappers[0]
        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.protocol).isEqualTo(protocol)
        assertThat(mapper.protocolMapper).isEqualTo(protocolMapper)
        assertThat(mapper.config["user.attribute"]).isEqualTo(userAttribute)
        assertThat(mapper.config["claim.name"]).isEqualTo(mapperName)
    }

    @Test
    fun testAddMapper_claimNameGiven() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val mapperName = "testMapper"
        val userAttribute = "testAttribute"
        val claimName = "claimName"
        AddUserAttributeMapperAction(testRealm, "simpleClient", mapperName, userAttribute,claimName = claimName).executeIt()

        val mappers = client.mappers(client.clientUUID("simpleClient", testRealm), testRealm)

        assertThat(mappers).hasSize(1)
        val mapper = mappers[0]
        assertThat(mapper.name).isEqualTo(mapperName)
        assertThat(mapper.config["claim.name"]).isEqualTo(claimName)
    }
}
