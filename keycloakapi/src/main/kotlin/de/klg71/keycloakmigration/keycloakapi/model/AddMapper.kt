package de.klg71.keycloakmigration.keycloakapi.model

data class AddMapper(val name: String, val config: Map<String, String>,
                              val protocol: String, val protocolMapper: String)

@Suppress("LongParameterList")
fun groupMembershipMapper(name: String, addToAccessToken: Boolean, addToIdToken: Boolean,
                                   fullGroupPath: Boolean,
                                   addToUserInfo: Boolean,
                                   claimName: String) =
        AddMapper(name, mapOf(
                "access.token.claim" to addToAccessToken.toString(),
                "id.token.claim" to addToIdToken.toString(),
                "full.path" to fullGroupPath.toString(),
                "userinfo.token.claim" to addToUserInfo.toString(),
                "claim.name" to claimName
        ), "openid-connect", "oidc-group-membership-mapper")

@Suppress("LongParameterList")
fun userRealmRoleMapper(name: String, addToAccessToken: Boolean, addToIdToken: Boolean,
                                 addToUserInfo: Boolean,
                                 claimName: String,
                                 prefix: String) =
        AddMapper(name, mapOf(
                "access.token.claim" to addToAccessToken.toString(),
                "id.token.claim" to addToIdToken.toString(),
                "userinfo.token.claim" to addToUserInfo.toString(),
                "multivalued" to "true",
                "jsonType.label" to "String",
                "claim.name" to claimName,
                "usermodel.realmRoleMapping.rolePrefix" to prefix
        ), "openid-connect", "oidc-usermodel-realm-role-mapper")

@Suppress("LongParameterList")
fun audienceMapper(name: String, addToAccessToken: Boolean, addToIdToken: Boolean,
                            clientAudience: String, customAudience: String) =
        AddMapper(name, mapOf(
                "access.token.claim" to addToAccessToken.toString(),
                "id.token.claim" to addToIdToken.toString(),
                "included.client.audience" to clientAudience,
                "included.custom.audience" to customAudience
        ), "openid-connect", "oidc-audience-mapper")

@Suppress("LongParameterList")
fun userAttributeMapper(name: String, addToAccessToken: Boolean, addToIdToken: Boolean,
                                 addToUserInfo: Boolean,
                                 claimName: String,
                                 aggregateAttributeValues: Boolean,
                                 multivalued: Boolean,
                                 userAttribute: String) =
        AddMapper(name, mapOf(
                "access.token.claim" to addToAccessToken.toString(),
                "id.token.claim" to addToIdToken.toString(),
                "userinfo.token.claim" to addToUserInfo.toString(),
                "multivalued" to multivalued.toString(),
                "jsonType.label" to "String",
                "claim.name" to claimName,
                "aggregate.attrs" to aggregateAttributeValues.toString(),
                "user.attribute" to userAttribute
        ), "openid-connect", "oidc-usermodel-attribute-mapper")
