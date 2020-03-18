package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.Group
import de.klg71.keycloakmigration.model.UpdateGroup
import de.klg71.keycloakmigration.rest.existsGroup
import de.klg71.keycloakmigration.rest.groupByName

class UpdateGroupAction(
        realm: String? = null,
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
