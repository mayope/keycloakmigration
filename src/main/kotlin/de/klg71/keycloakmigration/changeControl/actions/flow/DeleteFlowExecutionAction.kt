package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.executionsToImport
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowInPlace
import de.klg71.keycloakmigration.keycloakapi.updateFlowInPlace

class DeleteFlowExecutionAction(
    realm: String?,
    private val flowAlias: String,
    private val provider: String,
) : Action(realm) {

    private var oldFlow: Flow? = null
    private val oldExecutions: MutableList<AuthenticationExecutionImport> = mutableListOf()

    override fun execute() {
        client.flows(realm())
            .firstOrNull { it.alias == flowAlias }?.also {
                oldFlow = it
                oldExecutions.addAll(client.executionsToImport(realm(), flowAlias))
            }?.let {
                val flowExecution = oldExecutions.firstOrNull { it.providerId == provider }
                    ?: throw MigrationException("No flow execution found with providerId: $provider")

                val executionId = flowExecution.id ?: throw MigrationException("Flow execution ID is null")

                client.deleteFlowExecution(realm(), executionId)
            }
    }

    override fun undo() {
        val updateFlow = oldFlow ?: error("undo called but oldFlow is null")
        if (oldExecutions.isEmpty()) error("undo called but Executions is empty")

        client.flows(realm())
            .firstOrNull { it.alias == flowAlias }?.let {
                client.updateFlowInPlace(
                    realm(), it.alias, UpdateFlowInPlace(
                        updateFlow.alias,
                        updateFlow.description,
                        updateFlow.providerId,
                        updateFlow.topLevel,
                        oldExecutions
                    )
                )
            }
    }

    override fun name() = "DeleteFlowExecution [$provider] to $flowAlias"
}
