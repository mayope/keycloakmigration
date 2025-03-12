package de.klg71.keycloakmigration.changeControl.actions.realm.profile

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttribute
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttributePermissions
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttributeRequired
import de.klg71.keycloakmigration.keycloakapi.model.RealmProfile
import de.klg71.keycloakmigration.keycloakapi.realmExistsById

@Suppress("LongParameterList")
class AddRealmProfileAttributeAction(
    realm: String?,
    private val name: String,
    private val displayName: String? = null,
    private val annotations: Map<String, Any> = emptyMap(),
    private val validations: Map<String, Map<String, Any>> = emptyMap(),
    private val permissions: RealmAttributePermissions = RealmAttributePermissions(emptySet(), emptySet()),
    private val required: RealmAttributeRequired = RealmAttributeRequired(emptySet(), emptySet()),
    private val multivalued: Boolean = false
) : Action(realm) {

    private var oldRealmProfile: RealmProfile? = null

    override fun execute() {
        if (!client.realmExistsById(realm())) {
            throw MigrationException("Realm with id: ${realm()} does not exist!")
        }

        val realmProfile = client.realmUserProfile(realm())

        val mapper = jacksonObjectMapper()
        oldRealmProfile = mapper.readValue(mapper.writeValueAsString(realmProfile), RealmProfile::class.java)

        val realmAttribute: RealmAttribute? = realmProfile.attributes.find { it.name == name }

        if (realmAttribute != null) throw MigrationException("Realm profile attribute with name: $name already exists!")

        realmProfile.attributes.add(
            RealmAttribute(
                name,
                displayName,
                annotations,
                validations,
                RealmAttributePermissions(permissions.view ?: emptySet(), permissions.edit ?: emptySet()),
                RealmAttributeRequired(required.roles ?: emptySet(), required.scopes ?: emptySet()),
                multivalued
            )
        )

        client.updateRealmProfile(realm(), realmProfile)
    }

    override fun undo() {
        val realmProfile = oldRealmProfile ?: error("undo called but oldRealmProfile is null")
        client.updateRealmProfile(realm(), realmProfile)
    }

    override fun name() = "AddRealmProfileAttributeAction $name"
}
