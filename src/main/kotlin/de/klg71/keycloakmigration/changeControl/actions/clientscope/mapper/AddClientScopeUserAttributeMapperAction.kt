package de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper

import de.klg71.keycloakmigration.keycloakapi.model.userAttributeMapper

open class AddClientScopeUserAttributeMapperAction(
    realm: String?,
    name: String,
    clientScope: String,
    private val userAttribute: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val addToUserInfo: Boolean = true,
    private val claimName: String? = null,
    private val multivalued: Boolean = false,
    /** Indicates whether user attributes should be aggregated with the group attributes */
    private val aggregateAttributeValues: Boolean = true
) : AddClientScopeMapperAction(realm, clientScope, name) {

    override fun createMapper() = userAttributeMapper(
        name, addToAccessToken, addToIdToken, addToUserInfo, claimName ?: name,
        aggregateAttributeValues, multivalued, userAttribute
    )

    override fun name() = "AddClientScopeUserAttributeMapper $name to $clientScope"

}

