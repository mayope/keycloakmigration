package de.klg71.keycloakmigration.changeControl.actions.realm.profile

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttribute
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttributePermissions
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttributeRequired
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class UpdateRealmProfileAttributeIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testUpdateRealmProfileAttribute() {
        AddRealmProfileAttributeAction(
            testRealm,
            name = "country",
            required = RealmAttributeRequired(roles = setOf("admin", "user"))
        ).executeIt()

        UpdateRealmProfileAttributeAction(
            testRealm,
            name = "country",
            permissions = RealmAttributePermissions(view = setOf("admin", "user"))
        ).executeIt()

        val attribute = realmProfileAttribute("country")
        assertThat(attribute).isNotNull()
        assertThat(attribute?.required?.scopes).isEqualTo(emptySet<String>())
        assertThat(attribute?.required?.roles).isEqualTo(setOf("admin", "user"))
        assertThat(attribute?.permissions?.view).isEqualTo(setOf("admin", "user"))
        assertThat(attribute?.permissions?.edit).isEqualTo(emptySet<String>())
    }

    @Test
    fun testUpdateRealmProfileAttributeNotExisting() {
        assertThatThrownBy {
            UpdateRealmProfileAttributeAction(testRealm, name = "country").executeIt()
        }
            .isInstanceOf(MigrationException::class.java)
            .hasMessage("Realm profile attribute with name: country does not exist!")
    }

    fun realmProfileAttribute(name: String): RealmAttribute? {
        val realmProfile = client.realmUserProfile(testRealm)
        return realmProfile.attributes.find { it.name == name }
    }
}
