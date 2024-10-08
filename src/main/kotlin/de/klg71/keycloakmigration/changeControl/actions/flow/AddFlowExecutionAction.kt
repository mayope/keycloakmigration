package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.AddFlowExecution

class AddFlowExecutionAction(
    realm: String,
    private val flowAlias: String,
    private val provider: String,
    private val config: Map<String, String> = emptyMap()
) : Action(realm) {

    override fun execute() {
        client.addFlowExecution(realm(), flowAlias, AddFlowExecution(provider, config))
    }

    override fun undo() {
        TODO("Not yet implemented")
    }

    override fun name() = "AddFlowExecution [$provider] to $flowAlias"
}