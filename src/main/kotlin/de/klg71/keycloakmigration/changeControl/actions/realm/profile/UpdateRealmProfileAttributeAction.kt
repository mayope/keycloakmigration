package de.klg71.keycloakmigration.changeControl.actions.realm.profile

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttribute
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttributePermissions
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttributeRequired
import de.klg71.keycloakmigration.keycloakapi.model.RealmProfile
import de.klg71.keycloakmigration.keycloakapi.realmExistsById

class UpdateRealmProfileAttributeAction(
    realm: String?,
    private val name: String,
    private val displayName: String?,
    private val annotations: Map<String, Any>?,
    private val validations: Map<String, Map<String, Any>>?,
    private val permissions: RealmAttributePermissions?,
    private val required: RealmAttributeRequired?,
    private val multivalued: Boolean?
) : Action(realm) {

    private lateinit var oldRealmProfile: RealmProfile

    override fun execute() {
        if (!client.realmExistsById(realm())) {
            throw MigrationException("Realm with id: ${realm()} does not exist!")
        }

        val realmProfile = client.realmUserProfile(realm())

        val mapper = jacksonObjectMapper()
        oldRealmProfile = mapper.readValue(mapper.writeValueAsString(realmProfile), RealmProfile::class.java)

        val realmAttribute: RealmAttribute? = realmProfile.attributes.find { it.name == name }

        if (realmAttribute == null) throw MigrationException("Realm attribute with name: $name does not exist!")

        realmAttribute.let {
            it.name = name
            it.displayName = displayName ?: realmAttribute.displayName
            it.annotations = annotations ?: realmAttribute.annotations
            it.validations = validations ?: realmAttribute.validations
            it.permissions = permissions ?: realmAttribute.permissions
            it.required = required ?: realmAttribute.required
            it.multivalued = multivalued ?: realmAttribute.multivalued
        }

        client.updateRealmProfile(realm(), realmProfile)
    }

    override fun undo() {
        client.updateRealmProfile(realm(), oldRealmProfile)
    }

    override fun name() = "UpdateRealmProfileAttributeAction $name"
}
