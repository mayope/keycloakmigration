package de.klg71.keycloakmigration.changeControl.actions.realmprofile

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttribute
import de.klg71.keycloakmigration.keycloakapi.model.RealmProfile
import de.klg71.keycloakmigration.keycloakapi.realmExistsById


@Suppress("LongParameterList")
class AddRealmProfileAttributeAction(
    realm: String?,
    private val name: String,
    private val displayName: String?,
    private val annotations: Map<String, Any>?,
    private val validations: Map<String, Map<String, Any>>?,
    private val permissions: Map<String, List<String>>?,
    private val multivalued: Boolean?
) : Action(realm) {

    private var oldRealmProfile: RealmProfile? = null

    override fun execute() {
        if (!client.realmExistsById(realm())) {
            throw MigrationException("Realm with id: ${realm()} does not exist!")
        }

        val realmProfile = client.realmUserProfile(realm())

        val mapper = jacksonObjectMapper()
        oldRealmProfile = mapper.readValue(mapper.writeValueAsString(realmProfile), RealmProfile::class.java)

        realmProfile.attributes.add(
            RealmAttribute(
                name,
                displayName,
                annotations ?: emptyMap(),
                validations ?: emptyMap(),
                permissions ?: emptyMap(),
                multivalued ?: false
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
