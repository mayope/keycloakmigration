package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.copyAuthFlow
import java.util.UUID

class CopyFlowAction(
    realm: String,
    private val flowAlias: String
) : Action(realm) {

    private lateinit var flowUuid: UUID

    override fun execute() {
        flowUuid = client.copyAuthFlow(realm(), flowAlias)
    }

    override fun undo() {
        client.deleteFlow(realm(), flowUuid)
    }

    override fun name() = "CopyFlow $flowAlias"
}