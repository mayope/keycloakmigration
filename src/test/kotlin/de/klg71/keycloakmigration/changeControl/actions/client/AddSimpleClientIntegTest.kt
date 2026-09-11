package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class AddSimpleClientIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddClient() {
        AddSimpleClientAction(testRealm, "simpleClient", true, mapOf("test" to "1", "test2" to "2")).executeIt()

        val testClient = client.clientById("simpleClient", testRealm)

        assertThat(testClient.enabled).isEqualTo(true)
        assertThat(testClient.attributes).isEqualTo(
            mapOf(
                "backchannel.logout.revoke.offline.tokens" to "false", "backchannel.logout.session.required" to "true",
                "realm_client" to "false",
                "test" to "1", "test2" to "2"
            )
        )
    }

    @Test
    fun testAddClient_withSecret() {
        AddSimpleClientAction(
            testRealm, "simpleClient", true, mapOf("test" to "1", "test2" to "2"), secret = "testSecret",
            publicClient = false
        ).executeIt()

        val testClient = client.clientById("simpleClient", testRealm)

        assertThat(testClient.enabled).isEqualTo(true)
        assertThat(testClient.attributes).isEqualTo(
            mapOf(
                "backchannel.logout.revoke.offline.tokens" to "false", "backchannel.logout.session.required" to "true",
                "realm_client" to "false",
                "test" to "1", "test2" to "2"
            )
        )
        assertThat(testClient.publicClient).isEqualTo(false)
        val secret = client.clientSecret(testClient.id, testRealm)
        assertThat(secret.type).isEqualTo("secret")
        assertThat(secret.value).isEqualTo("testSecret")
    }

    @Test
    fun testAddClient_isIdempotentWhenClientAlreadyExists() {
        // First add creates the client.
        AddSimpleClientAction(testRealm, "simpleClient", true, mapOf("test" to "1")).executeIt()
        val created = client.clientById("simpleClient", testRealm)

        // Re-running the same add (as happens when a changeset is replayed after a
        // half-applied prior run) must NOT throw on the 409, and must adopt the
        // existing client rather than deleting or duplicating it.
        AddSimpleClientAction(testRealm, "simpleClient", true, mapOf("test" to "1")).executeIt()

        val afterReRun = client.clientById("simpleClient", testRealm)
        assertThat(afterReRun.id).isEqualTo(created.id)
        assertThat(afterReRun.enabled).isEqualTo(true)
        // The adopted client is left intact — the re-run's undo path must not have
        // fired, and the original client is still present with a single instance.
        assertThat(client.clients(testRealm).filter { it.clientId == "simpleClient" }).hasSize(1)
    }

    @Test
    fun testAddClientWithAuthorizationEnabled() {
        AddSimpleClientAction(
            testRealm,
            "simpleClient",
            true,
            authorizationServicesEnabled = true,
            serviceAccountsEnabled = true,
            publicClient = false
        ).executeIt()

        val testClient = client.clientById("simpleClient", testRealm)

        assertThat(testClient.enabled).isEqualTo(true)
        assertThat(testClient.serviceAccountsEnabled).isEqualTo(true)
        assertThat(testClient.authorizationServicesEnabled).isEqualTo(true)
    }
}
