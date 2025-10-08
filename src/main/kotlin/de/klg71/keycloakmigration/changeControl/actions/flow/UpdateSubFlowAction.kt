package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowExecution

class UpdateSubFlowAction(
    realm: String?,
    private val subFlow: String,
    private val topLevelFlow: String,
    private val requirement: Flow.Requirement
) : Action(realm) {

    private lateinit var original: AuthenticationExecution

    override fun execute() {
        if (client.flows(realm()).none { it.alias == topLevelFlow })
            throw MigrationException("Flow $topLevelFlow does not exist!")

        val flow = client.flowExecutions(realm(), topLevelFlow)
            .firstOrNull { it.displayName == subFlow && it.authenticationFlow }

        if (flow == null) throw MigrationException("SubFlow $subFlow doesn't exist for $topLevelFlow!")

        original = flow

        val updateFlowExecution = UpdateFlowExecution(
            original.id,
            requirement,
            original.level,
            original.index,
            original.priority,
            original.providerId
        )

        client.updateFlowExecution(
            realm(),
            topLevelFlow,
            updateFlowExecution
        )
    }

    override fun undo() {
        val updateFlowExecution = UpdateFlowExecution(
            original.id,
            original.requirement,
            original.level,
            original.index,
            original.priority,
            original.providerId
        )

        client.updateFlowExecution(realm(), topLevelFlow, updateFlowExecution)
    }

    override fun name() = "UpdateSubFlow $subFlow to $requirement"
}
