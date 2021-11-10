package de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper

import de.klg71.keycloakmigration.keycloakapi.model.userRealmRoleMapper

@Suppress("LongParameterList")
open class AddClientScopeUserRealmRoleMapperAction(
    realm: String?,
    clientScopeName: String,
    name: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val addToUserInfo: Boolean = true,
    private val claimName: String? = null,
    private val prefix: String = ""
) : AddClientScopeMapperAction(realm, clientScopeName, name) {

    override fun createMapper() = userRealmRoleMapper(
        name, addToAccessToken, addToIdToken, addToUserInfo,
        claimName ?: name, prefix
    )

    override fun name() = "AddClientScopeUserRealmRoleMapper $name to $clientScopeName"

}

