package de.klg71.keycloakmigration.changeControl.actions.realm

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.AddRealm
import de.klg71.keycloakmigration.rest.realmExistsById
import org.apache.commons.codec.digest.DigestUtils

class AddRealmAction(
        private val name: String,
        private val enabled: Boolean = true,
        private val id: String? = null) : Action() {

    private fun addRealm() = AddRealm(name, enabled, id())

    override fun execute() {
        if (client.realmExistsById(id())) {
            throw MigrationException("Realm with id: ${id()} already exists!")
        }
        client.addRealm(addRealm())
    }

    override fun undo() {
        client.deleteRealm(id())
    }

    private fun id() = id ?: name

    override fun name() = "AddRealm $name"

}