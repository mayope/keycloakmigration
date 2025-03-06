package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

data class AddUserFederationMapper(
        val name: String,
        val config: Map<String, List<String>>,
        val parentId: String,
        val providerId: String,
        val providerType: String
)

const val LDAP_STORAGE_MAPPER = "org.keycloak.storage.ldap.mappers.LDAPStorageMapper"

const val HARDCODED_LDAP_ROLE_MAPPER = "hardcoded-ldap-role-mapper"
fun hardcodedRoleMapper(name: String, role: String, parentId: String): AddUserFederationMapper {
    return AddUserFederationMapper(name, translateConfig(mapOf("role" to role)), parentId, HARDCODED_LDAP_ROLE_MAPPER,
            LDAP_STORAGE_MAPPER)
}

const val USER_ACCOUNT_CONTROL_MAPPER = "msad-user-account-control-mapper"
fun userAccountControlMapper(name: String, parentId: String): AddUserFederationMapper {
    return AddUserFederationMapper(name, translateConfig(emptyMap()), parentId, USER_ACCOUNT_CONTROL_MAPPER,
            LDAP_STORAGE_MAPPER)
}

const val USER_ATTRIBUTE_MAPPER = "user-attribute-ldap-mapper"
@Suppress("LongParameterList")
fun userAttributeMapper(name: String, parentId: String, userModelAttribute: String,
                                 ldapAttribute: String, readOnly: Boolean, alwaysReadFromLdap: Boolean,
                                 isMandatoryInLdap: Boolean): AddUserFederationMapper {
    return AddUserFederationMapper(name, translateConfig(mapOf(
            "user.model.attribute" to userModelAttribute,
            "ldap.attribute" to ldapAttribute,
            "read.only" to readOnly.toString(),
            "always.read.value.from.ldap" to alwaysReadFromLdap.toString(),
            "is.mandatory.in.ldap" to isMandatoryInLdap.toString()
    )), parentId, USER_ATTRIBUTE_MAPPER,
            LDAP_STORAGE_MAPPER)
}

const val GROUP_MAPPER = "group-ldap-mapper"
@Suppress("LongParameterList")
fun groupMapper(name: String, parentId: String, groupNameLdapAttribute: String,
                         groupObjectClasses: List<String>, groupsDn: String, preserveGroupInheritance: Boolean,
                         membershipLdapAttribute: String, membershipAttributeType: String,
                         membershipUserLdapAttribute: String,
                         filter: String, mode: String, ignoreMissingGroups: Boolean,
                         userRolesRetrieveStrategy: String, mappedGroupAttributes: List<String>,
                         memberofLdapAttribute: String,
                         dropNonExistingGroupsDuringSync: Boolean,
groupsPath:String): AddUserFederationMapper {
    return AddUserFederationMapper(name, translateConfig(mapOf(
            "group.name.ldap.attribute" to groupNameLdapAttribute,
            "group.object.classes" to groupObjectClasses.joinToString(separator = ","),
            "groups.dn" to groupsDn,
            "preserve.group.inheritance" to preserveGroupInheritance.toString(),
            "groups.ldap.filter" to filter,
            "membership.ldap.attribute" to membershipLdapAttribute,
            "membership.attribute.type" to membershipAttributeType,
            "membership.user.ldap.attribute" to membershipUserLdapAttribute,
            "mode" to mode,
            "ignore.missing.groups" to ignoreMissingGroups.toString(),
            "user.roles.retrieve.strategy" to userRolesRetrieveStrategy,
            "mapped.group.attributes" to mappedGroupAttributes.joinToString(separator = ","),
            "memberof.ldap.attribute" to memberofLdapAttribute,
            "drop.non.existing.groups.during.sync" to dropNonExistingGroupsDuringSync.toString(),
            "groups.path" to groupsPath
    )), parentId, GROUP_MAPPER, LDAP_STORAGE_MAPPER)
}

const val FULL_NAME_MAPPER = "full-name-ldap-mapper"
fun fullNameMapper(name: String, parentId: String, ldapFullNameAttribute: String, readOnly: Boolean,
                            writeOnly: Boolean): AddUserFederationMapper {
    return AddUserFederationMapper(name, translateConfig(mapOf(
            "ldap.full.name.attribute" to ldapFullNameAttribute,
            "read.only" to readOnly.toString(),
            "write.only" to writeOnly.toString()
    )), parentId, FULL_NAME_MAPPER, LDAP_STORAGE_MAPPER)
}

fun ldapMapper(name: String, config: Map<String, String>, parentId: String, providerId: String) =
        AddUserFederationMapper(name, translateConfig(config), parentId, providerId, LDAP_STORAGE_MAPPER)

fun translateConfig(config: Map<String, String>) =
        config.map { entry ->
            entry.key to listOf(entry.value)
        }.toMap()
