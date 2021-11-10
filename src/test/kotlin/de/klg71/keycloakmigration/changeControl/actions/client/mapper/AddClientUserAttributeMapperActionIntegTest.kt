package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class AddClientUserAttributeMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-usermodel-attribute-mapper"
    val userAttribute = "testAttribute"
    val claimName = "claimName"

    val clientId = "simpleClient"

    @Test
    fun testAddMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        AddClientUserAttributeMapperAction(
            testRealm, clientId, mapperName,
            userAttribute
        ).executeIt()

        val clientMappers = client.clientMappers(client.clientUUID(clientId, testRealm), testRealm)

        assertThat(clientMappers).hasSize(1)

        val clientMapper = clientMappers[0]

        assertThat(clientMapper.name).isEqualTo(mapperName)
        assertThat(clientMapper.protocol).isEqualTo(protocol)
        assertThat(clientMapper.protocolMapper).isEqualTo(protocolMapper)
        assertThat(clientMapper.config["user.attribute"]).isEqualTo(userAttribute)
        assertThat(clientMapper.config["claim.name"]).isEqualTo(mapperName)
    }

    @Test
    fun testAddMapperWithClaimName() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        AddClientUserAttributeMapperAction(
            testRealm, clientId, mapperName,
            userAttribute, claimName = claimName
        ).executeIt()

        val clientMappers = client.clientMappers(client.clientUUID(clientId, testRealm), testRealm)
        val clientMapper = clientMappers[0]

        assertThat(clientMapper.name).isEqualTo(mapperName)
        assertThat(clientMapper.config["claim.name"]).isEqualTo(claimName)
    }

}
