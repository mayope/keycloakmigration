package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.groupMembershipMapper

class AddGroupMembershipMapperAction(
        realm: String?,
        private val clientId: String?,
        private val clientScopeName: String?,
        private val name: String,
        private val addToIdToken: Boolean = true,
        private val addToAccessToken: Boolean = true,
        private val fullGroupPath: Boolean = true,
        private val addToUserInfo: Boolean = true,
        private val claimName: String? = null
) : AddMapperActionBase(realm, clientId, clientScopeName, name) {

    override fun createMapper() = groupMembershipMapper(name, addToAccessToken, addToIdToken, fullGroupPath,
            addToUserInfo,
            claimName ?: name)

    override fun name() = "AddGroupMembershipMapper $name to ${clientId ?: clientScopeName}"

}
