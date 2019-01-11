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
        AddRealmAction("test",id = "test").executeIt()

        Realm("test", "test", "test", true).let {
            assertThat(client.realmById("test")).isEqualTo(it)
        }

    }

    @Test
    fun testAddRealmExisting() {
        AddRealmAction("test",id="test").executeIt()
        assertThatThrownBy {
            AddRealmAction("test",id="test").executeIt()
        }
                .isInstanceOf(MigrationException::class.java)
                .hasMessage("Realm with id: test already exists!")
    }

    @After
    fun cleanup() {
        DeleteRealmAction("test").executeIt()
    }
}