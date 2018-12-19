package de.klg71.keycloakmigration.rest

import de.klg71.keycloakmigration.changeControl.KeycloakException
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.ClientListItem
import feign.Response
import java.util.*

fun KeycloakClient.userByName(name: String, realm: String) =
        searchByUsername(name, realm)
                .run {
                    if (isEmpty()) {
                        throw MigrationException("User with name: $name does not exist in $realm")
                    }
                    first()
                }
                .run {
                    user(id, realm)
                }

fun KeycloakClient.existsUserByName(name: String, realm: String): Boolean =
        searchByUsername(name, realm)
                .run {
                    if (isEmpty()) {
                        return false
                    }
                    return true
                }

fun KeycloakClient.existsRoleByName(name: String, realm: String): Boolean =
        checkRoleByName(name, realm)
                .run {
                    if (status() == 404) {
                        return false
                    }
                    return true
                }

fun KeycloakClient.clientById(clientId: String, realm: String): ClientListItem =
        clients(realm)
                .run {
                    if (isEmpty()) {
                        throw MigrationException("User with name: $clientId does not exist in $realm")
                    }
                    find { it.clientId == clientId }.let {
                        return it ?: throw MigrationException("Client with name $clientId does not exist in $realm")
                    }
                }

fun KeycloakClient.groupByName(name: String, realm: String) =
        searchGroup(name, realm)
                .run {
                    if (isEmpty()) {
                        throw MigrationException("Group with name: $name does not exist in $realm")
                    }
                    first()
                }

fun Response.isSuccessful() = when (status()) {
    in 200..299 -> true
    else -> false
}

fun Response.extractLocationUUID(): UUID {
    if (!isSuccessful()) {
        throw KeycloakException(this.body().asReader().readText())
    }
    return headers()["location"]!!.first()
            .run {
                split("/").last()
            }.let {
                UUID.fromString(it)
            }
}