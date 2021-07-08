package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.audienceMapper

class AddAudienceMapperAction(
        realm: String?,
        private val clientId: String?,
        private val clientScopeName: String?,
        private val name: String,
        private val addToIdToken: Boolean = true,
        private val addToAccessToken: Boolean = true,
        private val clientAudience: String = "",
        private val customAudience: String = ""
) : AddMapperActionBase(realm, clientId, clientScopeName, name) {

    override fun createMapper() = audienceMapper(name, addToAccessToken, addToIdToken, clientAudience, customAudience)

    override fun name() = "AddAudienceMapper $name to ${clientId ?: clientScopeName}"

}
