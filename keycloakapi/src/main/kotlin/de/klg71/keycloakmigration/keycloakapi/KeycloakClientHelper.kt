@file:Suppress("TooManyFunctions")

package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.Client
import de.klg71.keycloakmigration.keycloakapi.model.ClientScope
import de.klg71.keycloakmigration.keycloakapi.model.GroupListItem
import de.klg71.keycloakmigration.keycloakapi.model.Role
import de.klg71.keycloakmigration.keycloakapi.model.Organization
import feign.Response
import java.util.UUID

/**
 * File contains a lot of convenience functions when interacting with the keycloak client
 */

fun KeycloakClient.userByName(name: String, realm: String) =
    searchByUsername(name, realm, exact = true)
        .filter { it.username.lowercase() == name.lowercase() }
        .run {
            if (isEmpty()) {
                throw KeycloakApiException("User with name: $name does not exist in $realm!")
            }
            first()
        }
        .run {
            user(id, realm)
        }

fun KeycloakClient.userExists(user: UUID, realm: String) =
    tryUser(user, realm)
        .run {
            status() == 200
        }

fun KeycloakClient.clientById(clientId: String, realm: String): Client =
    clients(realm)
        .run {
            if (isEmpty()) {
                throw KeycloakApiException("Client with id: $clientId does not exist in $realm!")
            }
            find { it.clientId == clientId }.let {
                it ?: throw KeycloakApiException("Client with id: $clientId does not exist in realm: $realm!")
            }
        }.let {
            client(it.id, realm)
        }

fun KeycloakClient.isClientAuthorizationEnabled(clientId: String, realm: String): Boolean =
    clients(realm)
        .run {
            if (isEmpty()) {
                throw KeycloakApiException("Client with id: $clientId does not exist in $realm!")
            }
            find { it.clientId == clientId }?.let {
                return it.authorizationServicesEnabled
            }
            throw KeycloakApiException("Client with id: $clientId does not exist in realm: $realm!")
        }

fun KeycloakClient.clientScopeByName(name: String, realm: String): ClientScope =
    clientScopes(realm)
        .run {
            if (isEmpty()) {
                throw KeycloakApiException("ClientScope with name: $name does not exist in $realm!")
            }
            find { it.name == name }?.let {
                return it
            }
            throw KeycloakApiException("ClientScope with name: $name does not exist in realm: $realm!")
        }

fun KeycloakClient.existsClientScope(name: String, realm: String): Boolean =
    clientScopes(realm)
        .run {
            if (isEmpty()) {
                return false
            }
            find { it.name == name }?.let {
                return true
            }
            return false
        }

fun KeycloakClient.groupByName(name: String, realm: String) =
    searchGroup(name, realm)
        .run {
            if (isEmpty()) {
                throw KeycloakApiException("Group with name: $name does not exist in realm: $realm!")
            }
            searchByName(name) ?: throw KeycloakApiException("Group with name: $name does not exist in $realm")
        }.run {
            group(realm, id)
        }

@Suppress("ReturnCount")
fun KeycloakClient.existsGroup(name: String, realm: String): Boolean =
    searchGroup(name, realm)
        .run {
            if (isEmpty()) {
                return false
            }
            if (searchByName(name) == null) {
                return false
            }
            return true
        }

fun KeycloakClient.existsUser(name: String, realm: String): Boolean =
    searchUser(name, realm)
        .run {
            return !isEmpty()
        }

@Suppress("ReturnCount")
fun KeycloakClient.existsClient(clientId: String, realm: String): Boolean =
    clients(realm)
        .run {
            if (isEmpty()) {
                return false
            }
            find { it.clientId == clientId }?.let {
                return true
            }
            return false
        }

fun KeycloakClient.existsRole(name: String, realm: String): Boolean =
    roleByNameResponse(name, realm)
        .run {
            if (isSuccessful()) {
                return true
            }
            return false
        }

fun KeycloakClient.existsClientRole(name: String, realm: String, clientId: String): Boolean =
    clientRoles(realm, clientUUID(clientId, realm)).any {
        it.name == name
    }

private fun List<GroupListItem>.searchByName(name: String): GroupListItem? {
    return firstOrNull { it.name == name } ?: mapNotNull { it.subGroups.searchByName(name) }.firstOrNull()
}

fun KeycloakClient.clientRoleByName(name: String, clientId: String, realm: String): Role =
    clientById(clientId, realm)
        .run {
            clientRoles(realm, id)
        }.run {
            find { it.name == name }.let {
                if (it == null) {
                    throw KeycloakApiException(
                        "Role with name: $name does not exist on client $clientId on realm $realm!"
                    )
                }
                clientRole(it.id, realm, UUID.fromString(it.containerId))
            }
        }

fun KeycloakClient.userUUID(user: String, realm: String) = userByName(user, realm).id

fun KeycloakClient.groupUUID(group: String, realm: String) = groupByName(group, realm).id

fun KeycloakClient.clientUUID(clientId: String, realm: String) = clientById(clientId, realm).id

fun KeycloakClient.clientScopeUUID(clientScopeName: String, realm: String) = clientScopeByName(
    clientScopeName,
    realm
).id

internal const val SUCCESSFUL_RESPONSE_START = 200
internal const val SUCCESSFUL_RESPONSE_END = 299

fun Response.isSuccessful() = when (status()) {
    in SUCCESSFUL_RESPONSE_START..SUCCESSFUL_RESPONSE_END -> true
    else -> false
}

fun Response.extractLocationUUID(): UUID {
    if (!isSuccessful()) {
        throw KeycloakApiException(this.body().asReader().readText(), statusCode = status())
    }
    return headers()["location"]!!.first()
        .run {
            split("/").last()
        }.let {
            UUID.fromString(it)
        }
}

// realm's id is located in id field or realm field.
// only id field can't be used as a realm's id because there are cases
// where this field is a generic uuid, for example, Keycloak 20.0.3 initial master realm
fun KeycloakClient.realmById(id: String) =
    realms().firstOrNull { it.id == id || it.realm == id } ?: throw KeycloakApiException("Realm with id: $id does not exist!")

@Suppress("TooGenericExceptionCaught")
fun KeycloakClient.realmExistsById(id: String) =
    try {
        realms().any { it.id == id || it.realm == id }
    } catch (e: Throwable) {
        // If you don't have the right permissions you will only get the realmnames back
        realmNames().map { it.realm }.contains(id)
    }

fun KeycloakClient.roleExistsByName(name: String, realm: String) = roles(realm).any { it.name == name }

fun KeycloakClient.roleExistsByName(name: String, realm: String, client: String) = clientRoles(
    realm,
    clientById(client, realm).id
).any { it.name == name }

fun KeycloakClient.userFederationByName(name: String, realm: String) =
    userFederations(realm).firstOrNull { it.name == name }
        .let {
            it ?: throw KeycloakApiException("UserFederation with name: $name does not exist in $realm!")
        }

fun KeycloakClient.userFederationExistsByName(name: String, realm: String) =
    userFederations(realm).any { it.name == name }

fun KeycloakClient.identityProviderExistsByAlias(alias: String, realm: String) =
    identityProviders(realm).any { it.alias == alias }

fun KeycloakClient.clientMapperExistsByName(clientId: String, mapperName: String, realm: String) =
    clientMappers(clientUUID(clientId, realm), realm).any { it.name == mapperName }

fun KeycloakClient.clientScopeMapperExistsByName(clientScopeName: String, mapperName: String, realm: String) =
    clientScopeMappers(clientScopeUUID(clientScopeName, realm), realm).any { it.name == mapperName }

fun KeycloakClient.ldapMapperByName(ldapName: String, name: String, realm: String) =
    ldapMappers(realm, userFederationByName(ldapName, realm).id).firstOrNull { it.name == name }
        .let {
            it ?: throw KeycloakApiException(
                "UserFederationMapper with name: $name does not exist in $ldapName in $realm!"
            )
        }

fun KeycloakClient.ldapMapperExistsByName(ldapName: String, name: String, realm: String) =
    ldapMappers(realm, userFederationByName(ldapName, realm).id).any { it.name == name }

fun KeycloakClient.identityProviderByAlias(alias: String, realm: String) =
    identityProviders(realm).firstOrNull { it.alias == alias }
        .let {
            it ?: throw KeycloakApiException("IdentityProvider with alias: $alias does not exist in $realm!")
        }

fun KeycloakClient.identityProviderMapperByName(identityProviderAlias: String, name: String, realm: String) =
    identityProviderMappers(realm, identityProviderAlias).firstOrNull { it.name == name }
        .let {
            it ?: throw KeycloakApiException(
                "IdentityProviderMapper with name: $name does not exist in $identityProviderAlias in $realm!"
            )
        }

fun KeycloakClient.identityProviderMapperExistsByName(identityProviderAlias: String, name: String, realm: String) =
    identityProviderMappers(realm, identityProviderAlias).any { it.name == name }

fun KeycloakClient.organizationByName(name: String, realm: String): Organization = organizations(realm).run {
        if (isEmpty()) {
            throw KeycloakApiException("Organization with name: $name does not exist in $realm!")
        }
        find { it.name == name }?.let {
            return it
        }
        throw KeycloakApiException("Organization with name: $name does not exist in realm: $realm!")
    }