package de.klg71.keycloakmigration.changeControl.actions.clientscope.mappers

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.clientscope.AddClientScopeAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeMapperAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.DeleteClientScopeMapperAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientScopeUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class DeleteClientScopeMapperActionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    val config = mapOf(
        "userinfo.token.claim" to "true",
        "user.attribute" to "title",
        "id.token.claim" to "false",
        "access.token.claim" to "true",
        "claim.name" to "title",
        "jsonType.label" to "String"
    )
    val mapperName = "testMapper"
    val protocol = "openid-connect"
    val protocolMapper = "oidc-usermodel-attribute-mapper"

    val clientScopeName = "simpleClientScope"

    @Test
    fun testDeleteMapper() {
        AddClientScopeAction(testRealm, clientScopeName).executeIt()

        AddClientScopeMapperAction(
            testRealm, clientScopeName, mapperName,
            config, protocol, protocolMapper
        ).executeIt()

        DeleteClientScopeMapperAction(testRealm, clientScopeName, mapperName).executeIt()

        val mappers = client.clientScopeMappers(client.clientScopeUUID(clientScopeName, testRealm), testRealm)

        assertThat(mappers).hasSize(0)
    }
}
