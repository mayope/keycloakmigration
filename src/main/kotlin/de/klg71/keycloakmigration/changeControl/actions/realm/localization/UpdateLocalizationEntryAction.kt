package de.klg71.keycloakmigration.changeControl.actions.realm.localization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException

class UpdateLocalizationEntryAction(
    realm: String?,
    private val locale: String,
    private val key: String,
    private val text: String
) : Action(realm) {

    private var oldText: String? = null

    override fun execute() {
        oldText = client.getLocalizationEntries(realm(), locale)[key]

        oldText?.let {
            client.updateLocalizationEntry(realm(), locale, key, text)
        } ?: run {
            throw MigrationException("LocalizationEntry with $locale/$key does not exist!")
        }
    }

    override fun undo() {
        val text = oldText ?: error("undo called but oldText is null")
        client.updateLocalizationEntry(realm(), locale, key, text)
    }

    override fun name() = "UpdateLocalizationEntryAction $locale/$key"
}
