package de.klg71.keycloakmigration.changeControl.actions.realm

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.Realm
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.realmById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject

class AddRealmIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddRealm() {
        AddRealmAction("testRealm",id = "testRealm").executeIt()

        Realm("testRealm", "testRealm", null, true).let {
            assertThat(client.realmById("testRealm")).isEqualTo(it)
        }

    }

    @Test
    fun testAddRealmExisting() {
        AddRealmAction("testRealm",id="testRealm").executeIt()
        assertThatThrownBy {
            AddRealmAction("testRealm",id="testRealm").executeIt()
        }
                .isInstanceOf(MigrationException::class.java)
                .hasMessage("Realm with id: testRealm already exists!")
    }

    @After
    fun cleanup() {
        DeleteRealmAction("testRealm").executeIt()
    }
}