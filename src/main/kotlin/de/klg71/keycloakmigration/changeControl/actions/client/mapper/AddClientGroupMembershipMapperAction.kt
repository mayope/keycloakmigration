package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.groupMembershipMapper

// todo: remove `open` modifier when deprecation below gets removed!
open class AddClientGroupMembershipMapperAction(
    realm: String?,
    clientId: String,
    name: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val fullGroupPath: Boolean = true,
    private val addToUserInfo: Boolean = true,
    private val claimName: String? = null
) : AddClientMapperAction(realm, clientId, name) {

    override fun createMapper() = groupMembershipMapper(
        name, addToAccessToken, addToIdToken,
        fullGroupPath, addToUserInfo, claimName ?: name
    )

    override fun name() = "AddClientGroupMembershipMapper $name to $clientId"

}

@Deprecated("Will be removed in a future release. Use AddClientGroupMembershipMapperAction action instead")
class AddGroupMembershipMapperAction(
    realm: String?,
    clientId: String,
    name: String,
    addToIdToken: Boolean = true,
    addToAccessToken: Boolean = true,
    fullGroupPath: Boolean = true,
    addToUserInfo: Boolean = true,
    claimName: String? = null
) : AddClientGroupMembershipMapperAction(
    realm, clientId, name, addToIdToken, addToAccessToken, fullGroupPath, addToUserInfo, claimName
) {

    override fun name() = "AddGroupMembershipMapper $name to $clientId"

}
