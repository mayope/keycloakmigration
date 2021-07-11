package de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper

import de.klg71.keycloakmigration.keycloakapi.model.audienceMapper

open class AddClientScopeAudienceMapperAction(
    realm: String?,
    name: String,
    clientScope: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val clientAudience: String = "",
    private val customAudience: String = ""
) : AddClientScopeMapperAction(realm, clientScope, name) {

    override fun createMapper() = audienceMapper(name, addToAccessToken, addToIdToken, clientAudience, customAudience)

    override fun name() = "AddClientScopeAudienceMapper $name to $clientScope"

}

