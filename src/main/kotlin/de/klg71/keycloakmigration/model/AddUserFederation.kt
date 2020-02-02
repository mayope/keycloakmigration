package de.klg71.keycloakmigration.model

import de.klg71.keycloakmigration.changeControl.actions.MigrationException

data class AddUserFederation(val name: String,
                             val parentId: String,
                             val config: Map<String, List<String>>,
                             val providerId: String = "ldap",
                             val providerType: String = "org.keycloak.storage.UserStorageProvider")

fun constructAdLdapConfig(config: Map<String, String>): Map<String, List<String>> =
        mutableMapOf<String, List<String>>().apply {
            put("enabled", listOf("true"))
            put("priority", listOf(priority(config)))
            if (config["periodicFullSync"].equals("true", ignoreCase = true))
                put("fullSyncPeriod", listOf(fullSyncPeriod(config)))
            else
                put("fullSyncPeriod", listOf("-1"))

            if (config["periodicChangedUsersSync"].equals("true", ignoreCase = true))
                put("changedSyncPeriod", listOf(changedSyncPeriod(config)))
            else
                put("changedSyncPeriod", listOf("-1"))
            put("cachePolicy", listOf(cachePolicy(config)))
            put("evictionDay", listOf())
            put("evictionHour", listOf())
            put("evictionMinute", listOf())
            put("maxLifespan", listOf())
            put("batchSizeForSync", listOf(batchSize(config)))
            put("editMode", listOf(editMode(config)))
            put("importEnabled", listOf(importUsers(config)))
            put("syncRegistrations", listOf("false"))
            put("vendor", listOf("ad"))
            put("usernameLDAPAttribute", listOf(usernameLdapAttribute(config)))
            put("rdnLDAPAttribute", listOf(rdnLdapAttribute(config)))
            put("uuidLDAPAttribute", listOf(uuidLdapAttribute(config)))
            put("userObjectClasses", listOf(userObjectClasses(config)))
            put("connectionUrl", listOf(connectionUrl(config)))
            put("usersDn", listOf(usersDN(config)))
            put("authType", listOf(authenticationType(config)))
            put("bindDn", listOf(bindDN(config)))
            put("bindCredential", listOf(bindCredential(config)))
            put("customUserSearchFilter", listOf(ldapFilter(config)))
            put("searchScope", listOf(searchScope(config)))
            put("validatePasswordPolicy", listOf(validatePasswordPolicy(config)))
            put("useTruststoreSpi", listOf(useTruststoreSPI(config)))
            put("connectionPooling", listOf(connectionPooling(config)))
            put("connectionPoolingAuthentication", listOf())
            put("connectionPoolingDebug", listOf())
            put("connectionPoolingInitSize", listOf())
            put("connectionPoolingMaxSize", listOf())
            put("connectionPoolingPrefSize", listOf())
            put("connectionPoolingProtocol", listOf())
            put("connectionPoolingTimeout", listOf())
            put("connectionTimeout", listOf(connectionTimeout(config)))
            put("readTimeout", listOf(readTimeout(config)))
            put("pagination", listOf(pagination(config)))
            put("allowKerberosAuthentication", listOf(allowKerberosAuthentication(config)))
            put("serverPrincipal", listOf())
            put("keyTab", listOf())
            put("kerberosRealm", listOf())
            put("debug", listOf("false"))
            put("useKerberosForPasswordAuthentication", listOf(useKerberosAuthentication(config)))
        }

fun changedSyncPeriod(config: Map<String, String>) = config["changedSyncPeriod"] ?: "86400"

fun fullSyncPeriod(config: Map<String, String>) = config["fullSyncPeriod"] ?: "604800"

fun useKerberosAuthentication(config: Map<String, String>) = config["useKerberosAuthentication"] ?: "false"

fun allowKerberosAuthentication(config: Map<String, String>) = config["allowKerberosAuthentication"] ?: "false"

fun pagination(config: Map<String, String>) = config["pagination"] ?: "true"

fun readTimeout(config: Map<String, String>) = config["readTimeout"] ?: ""

fun connectionTimeout(config: Map<String, String>) = config["connectionTimeout"] ?: ""

fun connectionPooling(config: Map<String, String>) = config["connectionPooling"] ?: "true"

fun useTruststoreSPI(config: Map<String, String>) = config["useTruststoreSPI"] ?: "ldapsOnly"

fun validatePasswordPolicy(config: Map<String, String>) = config["validatePasswordPolicy"] ?: "false"

fun searchScope(config: Map<String, String>) = config["searchScope"] ?: "1"

fun ldapFilter(config: Map<String, String>) = config["ldapFilter"] ?: ""

fun bindCredential(config: Map<String, String>) = config["bindCredential"]
        ?: throw MigrationException("BindCredential on ldap not given!")

fun bindDN(config: Map<String, String>) = config["bindDn"] ?: throw MigrationException("BindDn on ldap not given!")

fun authenticationType(config: Map<String, String>) = config["authenticationType"] ?: "simple"

fun usersDN(config: Map<String, String>) = config["usersDn"] ?: throw MigrationException("UsersDn on ldap not given!")

fun connectionUrl(config: Map<String, String>) = config["connectionUrl"]
        ?: throw MigrationException("connectionUrl on ldap not given!")

fun userObjectClasses(config: Map<String, String>) = config["userObjectClasses"] ?: "person, organizationalPerson, user"

fun uuidLdapAttribute(config: Map<String, String>) = config["uuidLdapAttribute"] ?: "cn"

fun rdnLdapAttribute(config: Map<String, String>) = config["rdnLdapAttribute"] ?: "cn"

fun usernameLdapAttribute(config: Map<String, String>) = config["usernameLdapAttribute"] ?: "cn"

fun importUsers(config: Map<String, String>) = config["importUsers"] ?: "true"

fun editMode(config: Map<String, String>) = config["editMode"] ?: "READ_ONLY"

fun batchSize(config: Map<String, String>) = config["batchSize"] ?: "1000"

fun cachePolicy(config: Map<String, String>) = config["cachePolicy"] ?: "DEFAULT"

fun priority(config: Map<String, String>) = config["priority"] ?: "0"
