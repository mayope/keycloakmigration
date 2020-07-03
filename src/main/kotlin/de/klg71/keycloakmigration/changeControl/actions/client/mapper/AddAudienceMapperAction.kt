package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.audienceMapper
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import java.util.UUID

class AddAudienceMapperAction(
        realm: String?,
        private val clientId: String,
        private val name: String,
        private val addToIdToken: Boolean = true,
        private val addToAccessToken: Boolean = true,
        private val clientAudience: String = "",
        private val customAudience: String = ""
) : Action(realm) {

    private lateinit var mapperUuid: UUID

    override fun execute() {
        mapperUuid = addMapper(client, createMapper(), clientId, name, realm())
    }

    private fun createMapper() = audienceMapper(name, addToAccessToken, addToIdToken, clientAudience, customAudience)

    override fun undo() {
        client.clientUUID(clientId, realm()).let {
            client.deleteMapper(it, mapperUuid, realm())
        }
    }

    override fun name() = "AddAudienceMapper $clientId"

}
