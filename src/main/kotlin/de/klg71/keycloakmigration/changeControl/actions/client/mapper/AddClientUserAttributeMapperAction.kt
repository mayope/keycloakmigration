package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.userAttributeMapper

// todo: remove `open` modifier when deprecation below gets removed!
open class AddClientUserAttributeMapperAction(
    realm: String?,
    clientId: String,
    name: String,
    private val userAttribute: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val addToUserInfo: Boolean = true,
    private val claimName: String? = null,
    private val multivalued: Boolean = false,
    /** Indicates whether user attributes should be aggregated with the group attributes */
    private val aggregateAttributeValues: Boolean = true
) : AddClientMapperAction(realm, clientId, name) {

    override fun createMapper() = userAttributeMapper(
        name, addToAccessToken, addToIdToken, addToUserInfo, claimName ?: name,
        aggregateAttributeValues, multivalued, userAttribute
    )

    override fun name() = "AddClientUserAttributeMapper $name to $clientId"

}

@Deprecated("Will be removed in a future release. Use AddClientUserAttributeMapperAction action instead")
class AddUserAttributeMapperAction(
    realm: String?,
    clientId: String,
    name: String,
    userAttribute: String,
    addToIdToken: Boolean = true,
    addToAccessToken: Boolean = true,
    addToUserInfo: Boolean = true,
    claimName: String? = null,
    multivalued: Boolean = false,
    /** Indicates whether user attributes should be aggregated with the group attributes */
    aggregateAttributeValues: Boolean = true
): AddClientUserAttributeMapperAction(
    realm, clientId, name, userAttribute, addToIdToken, addToAccessToken, addToUserInfo,
    claimName, multivalued, aggregateAttributeValues
) {

    override fun name() = "AddUserAttributeMapper $name to $clientId"

}
