package de.klg71.keycloakmigration.changeControl.actions.realm.profile

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttribute
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddRealmProfileAttributeIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddRealmProfileAttribute() {
        AddRealmProfileAttributeAction(testRealm, name = "country").executeIt()
        val attribute = realmProfileAttribute("country")
        assertThat(attribute).isNotNull()
        assertThat(attribute?.required?.scopes).isNull()
        assertThat(attribute?.required?.roles).isNull()
        assertThat(attribute?.permissions?.view).isEqualTo(emptySet<String>())
        assertThat(attribute?.permissions?.edit).isEqualTo(emptySet<String>())
    }

    @Test
    fun testAddRealmProfileAttributeExisting() {
        AddRealmProfileAttributeAction(testRealm, name = "country").executeIt()
        assertThatThrownBy {
            AddRealmProfileAttributeAction(testRealm, name = "country").executeIt()
        }
            .isInstanceOf(MigrationException::class.java)
            .hasMessage("Realm profile attribute with name: country already exists!")
    }

    fun realmProfileAttribute(name: String): RealmAttribute? {
        val realmProfile = client.realmUserProfile(testRealm)
        return realmProfile.attributes.find { it.name == name }
    }
}
