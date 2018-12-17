package de.klg71.keycloakmigration.changeControl.actions

import de.klg71.keycloakmigration.KeycloakClient

fun KeycloakClient.getUserByName(name: String, realm: String) =
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
