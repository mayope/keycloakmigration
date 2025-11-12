package de.klg71.keycloakmigration.changeControl.actions.realm.profile

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttributeGroup

class AddRealmProfileAttributeGroupAction(
    realm: String?,
    private val name: String,
    private val displayName: String?,
    private val description: String?,
    private val annotations: Map<String, String> = emptyMap()
) : Action(realm) {

    override fun execute() {
        val profile = client.realmUserProfile(realm())

        val newGroup = RealmAttributeGroup(
            name,
            displayName,
            description,
            annotations
        )

        profile.groups.add(newGroup)

        client.updateRealmProfile(realm(), profile)
    }

    override fun undo() {
        val profile = client.realmUserProfile(realm())

        val group = RealmAttributeGroup(
            name, displayName, description, annotations
        )

        val indexToRemove = profile.groups.indexOfLast { it == group }

        profile.groups.removeAt(indexToRemove)

        client.updateRealmProfile(realm(), profile)
    }

    override fun name() = "AddRealmProfileAttributeGroup $name"
}
