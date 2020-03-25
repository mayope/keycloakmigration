package de.klg71.keycloakmigration.changeControl

import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.realmExistsById
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Checks if realms exist with internal cache
 */
class RealmChecker(realmsChecked: List<String> = listOf()) : KoinComponent {
    private val client by inject<KeycloakClient>()
    private val realmsChecked = realmsChecked.toMutableList()

    fun check(realm: String): String {
        if (realm in realmsChecked) {
            return realm
        }
        if (client.realmExistsById(realm)) {
            realmsChecked.add(realm)
            return realm
        }
        throw MigrationException("Realm with id: $realm does not exist!")
    }
}
