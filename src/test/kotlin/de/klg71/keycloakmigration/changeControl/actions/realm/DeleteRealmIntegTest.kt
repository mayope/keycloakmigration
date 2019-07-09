package de.klg71.keycloakmigration.changeControl.actions.realm

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.realmExistsById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.standalone.inject

class DeleteRealmIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteRealm(){
        AddRealmAction("testRealm",id = "testRealm").executeIt()
        DeleteRealmAction("testRealm").executeIt()

        assertThat(client.realmExistsById("testRealm")).isFalse()
    }

    @Test
    fun testDeleteRealmNotExisting(){
        assertThatThrownBy {
            DeleteRealmAction("testRealm").executeIt()
        }
                .isInstanceOf(MigrationException::class.java)
                .hasMessage("Realm with id: testRealm does not exist!")

    }
}