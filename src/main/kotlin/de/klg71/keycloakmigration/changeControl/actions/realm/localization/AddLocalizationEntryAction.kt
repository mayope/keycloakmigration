package de.klg71.keycloakmigration.changeControl.actions.realm.localization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException

class AddLocalizationEntryAction(
    realm: String?,
    private val locale: String,
    private val key: String,
    private val text: String
) : Action(realm) {

    override fun execute() {
        val entries = client.getLocalizationEntries(realm(), locale)

        entries[key]?.let {
            throw MigrationException("LocalizationEntry with $locale/$key already exists!")
        } ?: run {
            client.updateLocalizationEntry(realm(), locale, key, text)
        }
    }

    override fun undo() {
        client.deleteLocalizationEntry(realm(), locale, key)
    }

    override fun name() = "AddLocalizationEntryAction $locale/$key"
}
