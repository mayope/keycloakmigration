package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.AddFlowExecution

class AddFlowExecutionAction(
    realm: String,
    private val flowAlias: String,
    private val provider: String
) : Action(realm) {

    override fun execute() {
        var response = client.addFlowExecution(realm(), flowAlias, AddFlowExecution(provider))
        return
    }

    override fun undo() {
        TODO("Not yet implemented")
    }

    override fun name() = "AddFlowExecution [$provider] to $flowAlias"
}