package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AddGroup
import de.klg71.keycloakmigration.model.Group
import de.klg71.keycloakmigration.rest.groupByName
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class DeleteGroupAction(
        private val realm: String,
        private val name: String) : Action() {


    private fun addGroup() = AddGroup(name)
    lateinit var group: Group

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(name)
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        group = client.groupByName(name, realm)

        client.deleteGroup(group.id, realm)
    }

    private fun parentId(): UUID? =
            group.path.split("/").run {
                get(size - 2)
            }.let {
                client.groupByName(it, realm).id
            }

    override fun undo() {
        when (parentId() == null) {
            true -> client.addGroup(addGroup(), realm)
            false -> client.addChildGroup(addGroup(), parentId()!!, realm)
        }
    }

    override fun name() = "DeleteGroup $name"

}