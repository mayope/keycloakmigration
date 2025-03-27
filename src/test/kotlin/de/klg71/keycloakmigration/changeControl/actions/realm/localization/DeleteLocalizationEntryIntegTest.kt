package de.klg71.keycloakmigration.changeControl.actions.realm.localization

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class DeleteLocalizationEntryIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteLocalizationEntry() {
        AddLocalizationEntryAction(testRealm, locale = "en", key = "test", text = "Test").executeIt()
        DeleteLocalizationEntryAction(testRealm, locale = "en", key = "test").executeIt()

        assertThat(client.getLocalizationEntries(testRealm, "en")).doesNotContainKey("test")
    }

    @Test
    fun testDeleteLocalizationEntryNotExisting() {
        assertThatThrownBy {
            DeleteLocalizationEntryAction(testRealm, locale = "en", key = "fake-key").executeIt()
        }
            .isInstanceOf(MigrationException::class.java)
            .hasMessage("LocalizationEntry with en/fake-key does not exist!")
    }

}
