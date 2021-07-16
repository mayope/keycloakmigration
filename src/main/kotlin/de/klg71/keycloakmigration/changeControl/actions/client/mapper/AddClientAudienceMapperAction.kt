package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.audienceMapper

open class AddClientAudienceMapperAction(
    realm: String?,
    clientId: String,
    name: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val clientAudience: String = "",
    private val customAudience: String = ""
) : AddClientMapperAction(realm, clientId, name) {

    override fun createMapper() = audienceMapper(name, addToAccessToken, addToIdToken, clientAudience, customAudience)

    override fun name() = "AddClientAudienceMapper $name to $clientId"

}

@Deprecated("Will be removed in a future release. Use AddClientAudienceMapper action instead")
class AddAudienceMapperAction(
    realm: String?,
    clientId: String,
    name: String,
    addToIdToken: Boolean = true,
    addToAccessToken: Boolean = true,
    clientAudience: String = "",
    customAudience: String = ""
) : AddClientAudienceMapperAction(
    realm, clientId, name, addToIdToken, addToAccessToken, clientAudience, customAudience
) {

    override fun name() = "AddAudienceMapper $name to $clientId"

}
