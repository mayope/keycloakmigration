package de.klg71.keycloakmigration.changeControl.actions.realm.profile

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.RealmProfile

class AddRealmProfileAttributeGroupAction(
  realm: String?,
  private val name: String,
  private val displayName: String?,
  private val displayDescription: String?,
) : Action(realm) {
  override fun execute() {
    val currentProfile = client.realmUserProfile(realm())

    val newGroup = mapOf(
      "name" to name,
      "displayHeader" to (displayName ?: ""),
      "displayDescription" to (displayDescription ?: "")
    )

    val newProfile = RealmProfile(
      currentProfile.attributes,
      currentProfile.groups + newGroup,
      currentProfile.unmanagedAttributePolicy
    )

    client.updateRealmProfile(realm(), newProfile)
  }

  override fun undo() {
    val currentProfile = client.realmUserProfile(realm())

    val group = mapOf(
      "name" to name,
      "displayHeader" to (displayName ?: ""),
      "displayDescription" to (displayDescription ?: "")
    )

    val indexToRemove = currentProfile.groups.indexOfLast { it == group }

    val revertedGroups = currentProfile.groups.filterIndexed { index, _ -> index != indexToRemove }

    val newProfile = RealmProfile(currentProfile.attributes, revertedGroups, currentProfile.unmanagedAttributePolicy)

    client.updateRealmProfile(realm(), newProfile)
  }

  override fun name() = "AddRealmProfileAttributeGroup $name"
}