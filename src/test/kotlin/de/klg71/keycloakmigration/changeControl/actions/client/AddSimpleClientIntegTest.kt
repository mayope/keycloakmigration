package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject

class AddSimpleClientIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddClient() {
        AddSimpleClientAction(testRealm, "simpleClient", true, mapOf("test" to "1", "test2" to "2")).executeIt()

        val testClient = client.clientById("simpleClient", testRealm)

        assertThat(testClient.enabled).isEqualTo(true)
        assertThat(testClient.attributes).isEqualTo(mapOf("test" to "1", "test2" to "2"))
    }

    @Test
    fun testAddClient_withSecret() {
        AddSimpleClientAction(testRealm, "simpleClient", true, mapOf("test" to "1", "test2" to "2"),secret="testSecret", publicClient = false).executeIt()

        val testClient = client.clientById("simpleClient", testRealm)

        assertThat(testClient.enabled).isEqualTo(true)
        assertThat(testClient.attributes).isEqualTo(mapOf("test" to "1", "test2" to "2"))
        assertThat(testClient.publicClient).isEqualTo(false)
        val secret = client.clientSecret(testClient.id, testRealm)
        assertThat(secret.type).isEqualTo("secret")
        assertThat(secret.value).isEqualTo("testSecret")
    }
}
