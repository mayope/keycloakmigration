package de.klg71.keycloakmigration.changeControl.actions.realm.localization

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddLocalizationEntryIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddLocalizationEntry() {
        AddLocalizationEntryAction(testRealm, locale = "en", key = "test", text = "Test").executeIt()
        val entry = client.getLocalizationEntries(testRealm, "en")["test"]
        assertThat(entry).isEqualTo("Test")
    }

    @Test
    fun testAddLocalizationEntryExisting() {
        AddLocalizationEntryAction(testRealm, locale = "en", key = "test-existing", text = "Test Existing").executeIt()
        assertThatThrownBy {
            AddLocalizationEntryAction(
                testRealm, locale = "en", key = "test-existing", text = "Test Existing"
            ).executeIt()
        }
            .isInstanceOf(MigrationException::class.java)
            .hasMessage("LocalizationEntry with en/test-existing already exists!")
    }
}
