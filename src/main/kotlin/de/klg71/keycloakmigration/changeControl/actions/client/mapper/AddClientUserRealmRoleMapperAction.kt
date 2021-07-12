package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.userRealmRoleMapper

// todo: remove `open` modifier when deprecation below gets removed!
open class AddClientUserRealmRoleMapperAction(
    realm: String?,
    clientId: String,
    name: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val addToUserInfo: Boolean = true,
    private val claimName: String? = null,
    private val prefix: String = ""
) : AddClientMapperAction(realm, clientId, name) {

    override fun createMapper() = userRealmRoleMapper(
        name, addToAccessToken, addToIdToken, addToUserInfo,
        claimName ?: name, prefix
    )

    override fun name() = "AddClientUserRealmRoleMapper $name to $clientId"

}

@Deprecated("Will be removed in a future release. Use AddClientUserRealmRoleMapperAction action instead")
class AddUserRealmRoleMapperAction(
    realm: String?,
    clientId: String,
    name: String,
    addToIdToken: Boolean = true,
    addToAccessToken: Boolean = true,
    addToUserInfo: Boolean = true,
    claimName: String? = null,
    prefix: String = ""
) : AddClientUserRealmRoleMapperAction(
    realm, clientId, name, addToIdToken, addToAccessToken, addToUserInfo, claimName, prefix
) {

    override fun name() = "AddUserRealmRoleMapper $name to $clientId"

}
