package de.klg71.keycloakmigration.changeControl.actions.realm

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.Realm
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.realmById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.core.component.inject

class AddRealmIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddRealm() {
        AddRealmAction("testRealm", id = "testRealm").executeIt()
        val realm = client.realmById("testRealm")
        assertThat(realm.realm).isEqualTo("testRealm")
    }

    @Test
    fun testAddRealmExisting() {
        AddRealmAction("testRealm", id = "testRealm").executeIt()
        assertThatThrownBy {
            AddRealmAction("testRealm", id = "testRealm").executeIt()
        }
                .isInstanceOf(MigrationException::class.java)
                .hasMessage("Realm with id: testRealm already exists!")
    }

    @After
    fun cleanup() {
        DeleteRealmAction("testRealm").executeIt()
    }
}
