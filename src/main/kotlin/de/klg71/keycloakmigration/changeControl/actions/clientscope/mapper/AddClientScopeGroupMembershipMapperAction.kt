package de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper

import de.klg71.keycloakmigration.keycloakapi.model.groupMembershipMapper

open class AddClientScopeGroupMembershipMapperAction(
    realm: String?,
    name: String,
    clientScope: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val fullGroupPath: Boolean = true,
    private val addToUserInfo: Boolean = true,
    private val claimName: String? = null
) : AddClientScopeMapperAction(realm, name, clientScope) {

    override fun createMapper() = groupMembershipMapper(
        name, addToAccessToken, addToIdToken,
        fullGroupPath, addToUserInfo, claimName ?: name
    )

    override fun name() = "AddClientScopeGroupMembershipMapper $name to $clientScope"

}

