package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class DeleteClientMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val config = mapOf(
        "access.token.claim" to "true",
        "claim.name" to "email",
        "id.token.claim" to "true",
        "jsonType.label" to "String",
        "user.attribute" to "UserModel.getEmail()",
        "userinfo.token.claim" to "true"
    )
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-usermodel-property-mapper"

    val clientId = "simpleClient"

    @Test
    fun testDeleteMapper() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        AddClientMapperAction(
            testRealm, clientId, mapperName,
            config, protocol, protocolMapper
        ).executeIt()

        DeleteClientMapperAction(testRealm, clientId, mapperName).executeIt()

        val clientMappers = client.clientMappers(client.clientUUID(clientId, testRealm), testRealm)

        assertThat(clientMappers).hasSize(0)
    }
}
