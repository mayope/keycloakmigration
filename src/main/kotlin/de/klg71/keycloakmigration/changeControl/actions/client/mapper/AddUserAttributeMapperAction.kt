package de.klg71.keycloakmigration.changeControl.actions.client.mapper

import de.klg71.keycloakmigration.keycloakapi.model.userAttributeMapper
import java.util.*

class AddUserAttributeMapperAction(
    realm: String?,
    name: String,
    clientId: String? = null,
    clientScopeName: String? = null,
    private val userAttribute: String,
    private val addToIdToken: Boolean = true,
    private val addToAccessToken: Boolean = true,
    private val addToUserInfo: Boolean = true,
    private val claimName: String? = null,
    private val multivalued: Boolean = false,
    // Indicates whether user attributes should be aggregated with the group attributes
    private val aggregateAttributeValues: Boolean = true
) : AddMapperActionBase(realm, name, clientId, clientScopeName) {

    override fun createMapper() = userAttributeMapper(
        name, addToAccessToken, addToIdToken, addToUserInfo,
        claimName ?: name,
        aggregateAttributeValues, multivalued, userAttribute
    )

    override fun name() = "AddUserAttributeMapper $name to ${clientId ?: clientScopeName}"

}
