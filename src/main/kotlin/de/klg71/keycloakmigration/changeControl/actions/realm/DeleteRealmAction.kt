package de.klg71.keycloakmigration.changeControl.actions.realm

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.AddRealm
import de.klg71.keycloakmigration.model.Realm
import de.klg71.keycloakmigration.rest.realmById
import de.klg71.keycloakmigration.rest.realmExistsById
import org.apache.commons.codec.digest.DigestUtils

/**
 * Migration action for deleting realms
 *
 * INFO: Strange annotations have to be done because jackson fails on one argument constructors else
 */
class DeleteRealmAction @JsonCreator constructor(@JsonProperty("id") private val id: String) : Action() {

    private fun addRealm() = AddRealm(oldRealm.displayName ?: oldRealm.id, oldRealm.enabled, oldRealm.id)

    private lateinit var oldRealm: Realm

    override fun execute() {
        oldRealm = client.realmById(id)
        client.deleteRealm(id)
    }

    override fun undo() {
        client.addRealm(addRealm())
    }


    override fun name() = "DeleteRealm $id"

}