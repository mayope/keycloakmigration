package de.klg71.keycloakmigration.changeControl

import de.klg71.keycloakmigration.KeycloakClient

interface Action{
    fun execute(client:KeycloakClient)
    fun undo(client:KeycloakClient)
    fun isRequired(client:KeycloakClient):Boolean
    fun name():String
}

data class ChangeSet(val id: String,
                     val author: String,
                     val changes: List<Action>)