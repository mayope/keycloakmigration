package de.klg71.keycloakmigration.rest

import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import feign.Response

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
                    if (status()==404) {
                        return false
                    }
                    return true
                }

fun Response.isSuccessful() = when (status()) {
    in 200..299 -> true
    else -> false
}