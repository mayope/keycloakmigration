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
class UpdateRealmProfileAttributeAction(
    realm: String?,
    private val name: String,
    private val displayName: String? = null,
    private val annotations: Map<String, Any>? = null,
    private val validations: Map<String, Map<String, Any>>? = null,
    private val permissions: RealmAttributePermissions? = null,
    private val required: RealmAttributeRequired? = null,
    private val multivalued: Boolean? = null
) : Action(realm) {

    private lateinit var oldRealmProfile: RealmProfile

    @Suppress("CyclomaticComplexMethod")
    override fun execute() {
        if (!client.realmExistsById(realm())) {
            throw MigrationException("Realm with id: ${realm()} does not exist!")
        }

        val realmProfile = client.realmUserProfile(realm())

        val mapper = jacksonObjectMapper()
        oldRealmProfile = mapper.readValue(mapper.writeValueAsString(realmProfile), RealmProfile::class.java)

        val realmAttribute: RealmAttribute? = realmProfile.attributes.find { it.name == name }

        if (realmAttribute == null) throw MigrationException("Realm profile attribute with name: $name does not exist!")

        realmAttribute.let {
            it.name = name
            it.displayName = displayName ?: realmAttribute.displayName
            it.annotations = annotations ?: realmAttribute.annotations
            it.validations = validations ?: realmAttribute.validations
            it.permissions = if (permissions == null) realmAttribute.permissions else {
                RealmAttributePermissions(
                    permissions.view ?: realmAttribute.permissions.view,
                    permissions.edit ?: realmAttribute.permissions.edit
                )
            }
            it.required =
                if (required == null) realmAttribute.required
                else if (required.roles?.isEmpty() ?: false) null
                else {
                    RealmAttributeRequired(
                        required.roles ?: realmAttribute.required?.roles,
                        required.scopes ?: realmAttribute.required?.scopes
                    )
                }
            it.multivalued = multivalued ?: realmAttribute.multivalued
        }

        client.updateRealmProfile(realm(), realmProfile)
    }

    override fun undo() {
        client.updateRealmProfile(realm(), oldRealmProfile)
    }

    override fun name() = "UpdateRealmProfileAttributeAction $name"
}
