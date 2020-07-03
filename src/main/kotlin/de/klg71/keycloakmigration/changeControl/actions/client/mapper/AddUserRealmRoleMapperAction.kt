package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.userRealmRoleMapper
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import java.util.UUID

class AddUserRealmRoleMapperAction(
        realm: String?,
        private val clientId: String,
        private val name: String,
        private val addToIdToken: Boolean = true,
        private val addToAccessToken: Boolean = true,
        private val addToUserInfo: Boolean = true,
        private val claimName: String? = null,
        private val prefix: String = ""
) : Action(realm) {

    private lateinit var mapperUuid: UUID

    override fun execute() {
        mapperUuid = addMapper(client, createMapper(), clientId, name, realm())
    }

    private fun createMapper() =
            userRealmRoleMapper(name, addToAccessToken, addToIdToken, addToUserInfo, claimName ?: name, prefix)

    override fun undo() {
        client.clientUUID(clientId, realm()).let {
            client.deleteMapper(it, mapperUuid, realm())
        }
    }

    override fun name() = "AddGroupMembershipMember $clientId"

}
