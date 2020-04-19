package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.userAttributeMapper
import de.klg71.keycloakmigration.rest.clientUUID
import java.util.UUID

class AddUserAttributeMapperAction(
        realm: String?,
        private val clientId: String,
        private val name: String,
        private val userAttribute: String,
        private val addToIdToken: Boolean = true,
        private val addToAccessToken: Boolean = true,
        private val addToUserInfo: Boolean = true,
        private val claimName: String? = null,
        // Indicates whether user attributes should be aggregated with the group attributes
        private val aggregateAttributeValues: Boolean = true
) : Action(realm) {

    private lateinit var mapperUuid: UUID

    override fun execute() {
        mapperUuid = addMapper(client, createMapper(), clientId, name, realm())
    }


    private fun createMapper() = userAttributeMapper(name, addToAccessToken, addToIdToken, addToUserInfo,
            claimName ?: name,
            aggregateAttributeValues, userAttribute)

    override fun undo() {
        client.clientUUID(clientId, realm()).let {
            client.deleteMapper(it, mapperUuid, realm())
        }
    }

    override fun name() = "AddUserAttributeMapper $clientId"

}
