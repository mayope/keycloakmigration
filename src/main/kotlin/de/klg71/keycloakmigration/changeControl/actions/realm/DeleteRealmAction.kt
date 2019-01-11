package de.klg71.keycloakmigration.changeControl.actions.realm

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AddRealm
import de.klg71.keycloakmigration.model.Realm
import de.klg71.keycloakmigration.rest.realmById
import org.apache.commons.codec.digest.DigestUtils

/**
 * Migration action for deleting realms
 *
 * INFO: Strange annotations have to be done because jackson fails on one argument constructors else
 */
class DeleteRealmAction @JsonCreator constructor(@JsonProperty("name") private val name: String) : Action() {

    private val hash = calculateHash()
    private fun addRealm() = AddRealm(realm.displayName?:realm.id, realm.enabled, realm.id)

    private lateinit var realm: Realm

    private fun calculateHash() =
            StringBuilder().run {
                append(name)
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        realm = client.realmById(name)
        client.deleteRealm(name)
    }

    override fun undo() {
        client.addRealm(addRealm())
    }


    override fun name() = "DeleteRealm $name"

}