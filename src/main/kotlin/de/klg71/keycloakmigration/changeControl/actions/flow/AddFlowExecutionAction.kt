package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowExecution
import java.util.UUID

@Suppress("LongParameterList")
class AddFlowExecutionAction(
    realm: String?,
    private val flowAlias: String,
    private val provider: String,
    private val executionAlias: String,
    private val config: Map<String, String> = emptyMap(),
    private val requirement: Flow.Requirement? = null,
    private val priority: Int? = null
) : Action(realm) {

    private lateinit var executionId: UUID

    override fun execute() {
        executionId = client.addFlowExecution(realm(), flowAlias, AddFlowExecution(provider)).extractLocationUUID()

        if (requirement != null || priority != null) {
            val executions = client.flowExecutions(realm(), flowAlias)
            val execution = executions.first { it.id == executionId }

            client.updateFlowExecution(
                realm(),
                flowAlias,
                UpdateFlowExecution(
                    executionId,
                    requirement ?: execution.requirement,
                    execution.level,
                    execution.index,
                    priority ?: execution.priority,
                    execution.providerId
                )
            )
        }

        if (config.isNotEmpty())
            client.updateFlowExecutionWithNewConfiguration(
                realm(),
                executionId.toString(),
                AuthenticatorConfig(executionAlias, config)
            )
    }

    override fun undo() {
        client.deleteFlowExecution(realm(), executionId)
    }

    override fun name() = "AddFlowExecution [$provider] to $flowAlias"
}
