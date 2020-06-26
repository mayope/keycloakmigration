package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.clientById
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.koin.core.inject

class ImportClientIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testImportClient() {
        ImportClientAction(testRealm, "integ-test-client.json").apply {
            path="src/test/resources/changesets"
        }.executeIt()

        val testClient = client.clientById("integ-test-client", testRealm)

        assertThat(testClient.directAccessGrantsEnabled).isEqualTo(true)
        assertThat(testClient.protocolMappers).hasSize(7)
    }


    @Test
    fun testImportClientWebOrigins() {
        ImportClientAction(testRealm, "integ-test-client-weborigins.json").apply {
            path="src/test/resources/changesets"
        }.executeIt()

        val testClient = client.clientById("integ-test-client", testRealm)

        assertThat(testClient.webOrigins).containsExactly("+")
    }
}
