package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.Group
import de.klg71.keycloakmigration.model.UpdateGroup
import de.klg71.keycloakmigration.rest.existsGroup
import de.klg71.keycloakmigration.rest.groupByName
import org.apache.commons.codec.digest.DigestUtils

class UpdateGroupAction(
        realm:String?=null,
        private val name: String,
        private val attributes: Map<String, List<String>>?,
        private val realmRoles: List<String>?,
        private val clientRoles: Map<String, List<String>>?) : Action(realm) {

    private lateinit var group: Group


    private fun updateGroup() = UpdateGroup(name, group.path,
            attributes ?: group.attributes,
            //FIXME
            group.access!!,
            clientRoles ?: group.clientRoles,
            realmRoles ?: group.realmRoles,
            group.subGroups
    )

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm())
                append(name)
                attributes?.entries?.forEach {
                    append(it.key)
                    it.value.forEach {
                        append(it)
                    }
                }
                realmRoles?.forEach {
                    append(it)
                }
                clientRoles?.entries?.forEach {
                    append(it.key)
                    it.value.forEach {
                        append(it)
                    }
                }
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        if (!client.existsGroup(name, realm())) {
            throw MigrationException("Group with name: $name does not exists in realm: ${realm()}!")
        }

        group = client.groupByName(name, realm()).run {
            client.group(realm(), id)
        }

        client.updateGroup(updateGroup(), realm(), group.id)

    }

    override fun undo() {
        client.updateGroup(UpdateGroup(group.name, group.path, group.attributes, group.access!!,
                group.clientRoles, group.realmRoles, group.subGroups),
                realm(), group.id)
    }

    override fun name() = "UpdateGroup $name"

}