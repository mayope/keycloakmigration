package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.AddGroup
import de.klg71.keycloakmigration.rest.existsGroup
import de.klg71.keycloakmigration.rest.extractLocationUUID
import de.klg71.keycloakmigration.rest.groupByName
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class AddGroupAction(
        realm:String?=null,
        private val name: String,
        private val parent: String? = null) : Action(realm) {

    private lateinit var groupUUID: UUID

    private fun addGroup() = AddGroup(name)

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(name)
                append(parent)
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        if(client.existsGroup(name,realm())){
            throw MigrationException("Group with name: $name already exists in realm: ${realm()}!")
        }
        when (parent.isNullOrBlank()) {
            true -> client.addGroup(addGroup(), realm())
            false -> client.addChildGroup(addGroup(), parentId(), realm())
        }.run {
            groupUUID = extractLocationUUID()
        }

    }

    private fun parentId() = client.groupByName(parent!!, realm()).id

    override fun undo() {
        client.deleteGroup(groupUUID, realm())
    }

    override fun name() = "AddGroup $name"

}