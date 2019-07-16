package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.clientById
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject

class AddSimpleClientIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddClient() {
        AddSimpleClientAction(testRealm, "simpleClient", true, mapOf("test" to "1","test2" to "2")).executeIt()

        val testClient = client.clientById("simpleClient", testRealm)

        assertThat(testClient.enabled).isEqualTo(true)
        assertThat(testClient.attributes).isEqualTo(mapOf("test" to "1","test2" to "2"))
    }
}