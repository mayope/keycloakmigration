package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.userRealmRoleMapper

class AddUserRealmRoleMapperAction(
    realm: String?,
    private val clientId: String?,
    private val clientScopeName: String?,
    private val name: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val addToUserInfo: Boolean = true,
    private val claimName: String? = null,
    private val prefix: String = ""
) : AddMapperActionBase(realm, clientId, clientScopeName, name) {
    override fun createMapper() =
        userRealmRoleMapper(name, addToAccessToken, addToIdToken, addToUserInfo, claimName ?: name, prefix)

    override fun name() = "AddGroupMembershipMember $name to ${clientId ?: clientScopeName}"

}
