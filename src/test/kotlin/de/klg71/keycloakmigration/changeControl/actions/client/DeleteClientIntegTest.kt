package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class DeleteClientIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteClient() {
        AddSimpleClientAction(testRealm, "simpleClient").executeIt()
        DeleteClientAction(testRealm, "simpleClient").executeIt()

        assertThat(client.clients(testRealm).firstOrNull{it.name=="simpleClient"}).isNull()
    }
}
