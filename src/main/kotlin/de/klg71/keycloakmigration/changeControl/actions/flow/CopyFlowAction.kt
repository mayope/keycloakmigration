package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.copyAuthFlow
import java.util.UUID

class CopyFlowAction(
    realm: String,
    private val flowAlias: String,
    private val newName: String
) : Action(realm) {

    override fun execute() {
        client.copyAuthFlow(realm(), flowAlias, newName)
    }

    override fun undo() {
//        client.deleteFlow(realm(), flowUuid)
    }

    override fun name() = "CopyFlow $flowAlias"
}