package de.klg71.keycloakmigration.keycloakapi.model

data class AddIdentityProviderMapper(
    val config: Map<String, String>,
    val identityProviderAlias: String,
    val identityProviderMapper: String,
    val name: String
) {
    companion object {
        @JvmStatic
        fun from(mapper: IdentityProviderMapper) = AddIdentityProviderMapper(
            mapper.config,
            mapper.identityProviderAlias,
            mapper.identityProviderMapper,
            mapper.name
        )
    }
}

const val SAML_USER_ATTRIBUTE_IDP_MAPPER = "saml-user-attribute-idp-mapper"
const val SAML_ROLE_IDP_MAPPER = "saml-role-idp-mapper"

const val SAML_ATTRIBUTE_EMAILADDRESS = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress"
fun emailAddressMapper(identityProviderAlias: String, name: String, attributeName: String): AddIdentityProviderMapper {
    return AddIdentityProviderMapper(
            mapOf(
                    "syncMode" to "INHERIT",
                    "user.attribute" to attributeName,
                    "attributes" to "[]",
                    "attribute.name" to SAML_ATTRIBUTE_EMAILADDRESS
            ),
            identityProviderAlias,
            SAML_USER_ATTRIBUTE_IDP_MAPPER,
            name
    )
}

const val SAML_ATTRIBUTE_SURNAME = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname"
fun surnameMapper(identityProviderAlias: String, name: String, attributeName: String): AddIdentityProviderMapper {
    return AddIdentityProviderMapper(
            mapOf(
                    "syncMode" to "INHERIT",
                    "user.attribute" to attributeName,
                    "attributes" to "[]",
                    "attribute.name" to SAML_ATTRIBUTE_SURNAME
            ),
            identityProviderAlias,
            SAML_USER_ATTRIBUTE_IDP_MAPPER,
            name
    )
}

const val SAML_ATTRIBUTE_GIVENNAME = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname"
fun givenNameMapper(identityProviderAlias: String, name: String, attributeName: String): AddIdentityProviderMapper {
    return AddIdentityProviderMapper(
            mapOf(
                    "syncMode" to "INHERIT",
                    "user.attribute" to attributeName,
                    "attributes" to "[]",
                    "attribute.name" to SAML_ATTRIBUTE_GIVENNAME
            ),
            identityProviderAlias,
            SAML_USER_ATTRIBUTE_IDP_MAPPER,
            name
    )
}

const val SAML_ATTRIBUTE_NAME = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"
fun nameMapper(identityProviderAlias: String, name: String, attributeName: String): AddIdentityProviderMapper {
    return AddIdentityProviderMapper(
            mapOf(
                    "syncMode" to "INHERIT",
                    "user.attribute" to attributeName,
                    "attributes" to "[]",
                    "attribute.name" to SAML_ATTRIBUTE_NAME
            ),
            identityProviderAlias,
            SAML_USER_ATTRIBUTE_IDP_MAPPER,
            name
    )
}

const val SAML_ATTRIBUTE_ROLE = "http://schemas.microsoft.com/ws/2008/06/identity/claims/role"
fun roleMapper(identityProviderAlias: String, name: String, attributeValue: String, role: String): AddIdentityProviderMapper {
    return AddIdentityProviderMapper(
            mapOf(
                    "syncMode" to "INHERIT",
                    "attribute.value" to attributeValue,
                    "attributes" to "[]",
                    "role" to role,
                    "attribute.name" to SAML_ATTRIBUTE_ROLE
            ),
            identityProviderAlias,
            SAML_ROLE_IDP_MAPPER,
            name
    )
}
