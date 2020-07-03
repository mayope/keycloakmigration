package de.klg71.keycloakmigration.changeControl.actions.realm

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.realmExistsById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class DeleteRealmIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteRealm() {
        AddRealmAction("testRealm", id = "testRealm").executeIt()
        DeleteRealmAction("testRealm").executeIt()

        assertThat(client.realmExistsById("testRealm")).isFalse()
    }

    @Test
    fun testDeleteRealmNotExisting() {
        assertThatThrownBy {
            DeleteRealmAction("testRealm").executeIt()
        }.isInstanceOf(KeycloakApiException::class.java)
                .hasMessage("Realm with id: testRealm does not exist!")

    }
}
