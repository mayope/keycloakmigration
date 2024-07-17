package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.existsGroup
import de.klg71.keycloakmigration.keycloakapi.groupByName
import de.klg71.keycloakmigration.keycloakapi.model.Group
import de.klg71.keycloakmigration.keycloakapi.model.UpdateGroup

class RenameGroupAction(
    realm: String? = null,
    private val name: String,
    private val newName: String) : Action(realm) {

    private lateinit var group: Group

    private fun updateGroup() = UpdateGroup(newName, group.path, group.attributes,
        //FIXME
        group.access!!,
        group.clientRoles, group.realmRoles, group.subGroups
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

    override fun name() = "RenameGroup $name to $newName"

}
