package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.groupMembershipMapper
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import java.util.UUID

class AddGroupMembershipMapperAction(
        realm: String?,
        private val clientId: String,
        private val name: String,
        private val addToIdToken: Boolean = true,
        private val addToAccessToken: Boolean = true,
        private val fullGroupPath: Boolean = true,
        private val addToUserInfo: Boolean = true,
        private val claimName: String? = null
) : Action(realm) {

    private lateinit var mapperUuid: UUID

    override fun execute() {
        mapperUuid = addMapper(client, createMapper(), clientId, name, realm())
    }

    private fun createMapper() = groupMembershipMapper(name, addToAccessToken, addToIdToken, fullGroupPath,
            addToUserInfo,
            claimName ?: name)

    override fun undo() {
        client.clientUUID(clientId, realm()).let {
            client.deleteMapper(it, mapperUuid, realm())
        }
    }

    override fun name() = "AddGroupMembershipMapper $clientId"
}
