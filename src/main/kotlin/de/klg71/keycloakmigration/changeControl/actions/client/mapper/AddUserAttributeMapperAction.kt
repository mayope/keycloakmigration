package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.model.userAttributeMapper
import java.util.*

class AddUserAttributeMapperAction(
    realm: String?,
    private val clientId: String,
    private val name: String,
    private val userAttribute: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val addToUserInfo: Boolean = true,
    private val claimName: String? = null,
    private val multivalued: Boolean = false,
    // Indicates whether user attributes should be aggregated with the group attributes
    private val aggregateAttributeValues: Boolean = true
) : Action(realm) {

    private lateinit var mapperUuid: UUID

    override fun execute() {
        mapperUuid = addMapper(client, createMapper(), clientId, name, realm())
    }


    private fun createMapper() = userAttributeMapper(
        name, addToAccessToken, addToIdToken, addToUserInfo,
        claimName ?: name,
        aggregateAttributeValues, multivalued, userAttribute
    )

    override fun undo() {
        client.clientUUID(clientId, realm()).let {
            client.deleteMapper(it, mapperUuid, realm())
        }
    }

    override fun name() = "AddUserAttributeMapper $name to client: $clientId"

}
