package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.audienceMapper

class AddAudienceMapperAction(
        realm: String?,
        name: String,
        clientId: String? = null,
        clientScopeName: String? = null,
        private val addToIdToken: Boolean = true,
        private val addToAccessToken: Boolean = true,
        private val clientAudience: String = "",
        private val customAudience: String = ""
) : AddMapperActionBase(realm, name, clientId, clientScopeName) {

    override fun createMapper() = audienceMapper(name, addToAccessToken, addToIdToken, clientAudience, customAudience)

    override fun name() = "AddAudienceMapper $name to ${clientId ?: clientScopeName}"

}
