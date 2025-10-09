package de.klg71.keycloakmigration.changeControl.actions.realm.profile

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.RealmProfile
import de.klg71.keycloakmigration.keycloakapi.realmExistsById

class UpdateRealmProfileOrderAction(
    realm: String?,
    private val order: List<String>
) : Action(realm) {

    private lateinit var oldRealmProfile: RealmProfile

    override fun execute() {
        if (!client.realmExistsById(realm())) {
            throw MigrationException("Realm with id: ${realm()} does not exist!")
        }

        val realmProfile = client.realmUserProfile(realm())

        val mapper = jacksonObjectMapper()
        oldRealmProfile = mapper.readValue(mapper.writeValueAsString(realmProfile), RealmProfile::class.java)

        val existingAttributes = realmProfile.attributes

        // Check if all entries in the new order exist in the current profile
        order.forEach { attrName ->
            if (existingAttributes.none { it.name == attrName }) {
                throw MigrationException("Attribute '$attrName' does not exist in the realm profile!")
            }
        }

        // Check if there are any attributes in the profile missing from the new order
        val missingInOrder = existingAttributes.map { it.name }.filterNot { order.contains(it) }
        if (missingInOrder.isNotEmpty()) {
            throw MigrationException("Attributes missing in new order: $missingInOrder")
        }

        // Reorder attributes according to the order list
        val reorderedAttributes = order.map { attrName ->
            existingAttributes.first { it.name == attrName }
        }

        realmProfile.attributes.clear()
        realmProfile.attributes.addAll(ArrayList(reorderedAttributes))

        client.updateRealmProfile(realm(), realmProfile)
    }

    override fun undo() {
        client.updateRealmProfile(realm(), oldRealmProfile)
    }

    override fun name() = "UpdateRealmProfileOrderAction $realm"
}
