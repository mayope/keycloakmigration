package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.groupMembershipMapper

class AddGroupMembershipMapperAction(
        realm: String?,
        name: String,
        clientId: String? = null,
        clientScopeName: String? = null,
        private val addToIdToken: Boolean = true,
        private val addToAccessToken: Boolean = true,
        private val fullGroupPath: Boolean = true,
        private val addToUserInfo: Boolean = true,
        private val claimName: String? = null
) : AddMapperActionBase(realm, name, clientId, clientScopeName) {

    override fun createMapper() = groupMembershipMapper(name, addToAccessToken, addToIdToken, fullGroupPath,
            addToUserInfo,
            claimName ?: name)

    override fun name() = "AddGroupMembershipMapper $name to ${clientId ?: clientScopeName}"

}
