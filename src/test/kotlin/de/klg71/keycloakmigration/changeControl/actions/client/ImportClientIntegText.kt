package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.clientById
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject
import java.util.*

class ImportClientIntegText : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testImportClient() {
        ImportClientAction("master", "test-client.json").executeIt()

        val coco = client.clientById("test-client", "master")

        assertThat(coco.id).isEqualTo(UUID.fromString("f5631da6-514d-49b1-a423-0edd3d1d2402"))
        assertThat(coco.directAccessGrantsEnabled).isEqualTo(true)
        assertThat(coco.protocolMappers).hasSize(7)
    }

    @After
    fun cleanup() {
        DeleteClientAction("master", "test-client").executeIt()
    }
}