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

class DeleteMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteMapper() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        val config = mapOf(
                "access.token.claim" to "true",
                "claim.name" to "email",
                "id.token.claim" to "true",
                "jsonType.label" to "String",
                "user.attribute" to "UserModel.getEmail()",
                "userinfo.token.claim" to "true")
        val mapperName = "testMapper"
        val protocol = "openid-connect"
        val protocolMapper = "oidc-usermodel-property-mapper"
        AddMapperAction(testRealm, "simpleClient", mapperName, config,
                protocolMapper, protocol).executeIt()
        DeleteMapperAction(testRealm, "simpleClient", mapperName).executeIt()

        val mappers = client.mappers(client.clientUUID("simpleClient", testRealm), testRealm)

        assertThat(mappers).hasSize(0)
    }
}
