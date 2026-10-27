package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddSubFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowExecution
import feign.FeignException
import java.util.UUID

private val SUB_FLOW_TYPES = setOf("basic-flow", "form-flow")

/**
 * Adds a subflow to an existing flow. The parent can itself be a subflow, which nests one subflow in another.
 */
@Suppress("LongParameterList")
class AddSubFlowAction(
    realm: String?,
    private val flowAlias: String,
    private val alias: String,
    private val description: String = "",
    private val type: String = "basic-flow",
    private val provider: String? = null,
    private val requirement: Flow.Requirement? = null,
    private val priority: Int? = null
) : Action(realm) {

    private lateinit var executionId: UUID

    override fun execute() {
        if (type !in SUB_FLOW_TYPES)
            throw MigrationException("SubFlow type must be one of $SUB_FLOW_TYPES, got: $type")

        // flows() only lists top-level flows, so look the parent up directly to also accept a subflow
        try {
            client.flowExecutions(realm(), flowAlias)
        } catch (e: FeignException.NotFound) {
            throw MigrationException("Flow $flowAlias does not exist!", e)
        }

        client.addSubFlow(realm(), flowAlias, AddSubFlow(alias, description, type, provider)).run {
            if (!isSuccessful())
                throw MigrationException(
                    "Failed to add subflow $alias to $flowAlias in realm: ${realm()}. " +
                        "Status: ${status()}, Reason: ${reason()}"
                )
        }

        // A subflow's alias is unique in the realm and is its execution's display name
        val execution = subFlowExecution()
        executionId = execution.id
        setExecuted()

        if (requirement != null || priority != null) {
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
            ).run {
                if (!isSuccessful())
                    throw MigrationException(
                        "Failed to set requirement/priority of subflow $alias in $flowAlias. " +
                            "Status: ${status()}, Reason: ${reason()}"
                    )
            }
        }
    }

    private fun subFlowExecution(): AuthenticationExecution =
        client.flowExecutions(realm(), flowAlias).firstOrNull { it.authenticationFlow && it.displayName == alias }
            ?: throw MigrationException("SubFlow $alias was added but not found in $flowAlias!")

    /**
     * Deleting the subflow's execution also deletes the subflow and everything in it.
     */
    override fun undo() {
        client.deleteFlowExecution(realm(), executionId)
    }

    override fun name() = "AddSubFlow $alias to $flowAlias"
}
