package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.addExecutions
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddSubFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationFlowAction
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
    private val priority: Int? = null,
    private val executions: List<AuthenticationFlowAction> = emptyList()
) : Action(realm) {

    private var executionId: UUID? = null

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

        // The subflow now exists, so mark executed before anything else can fail: this guarantees the
        // orphaned subflow is rolled back even if the following retrieval or updates throw.
        setExecuted()

        val execution = subFlowExecution()
        executionId = execution.id

        // The create endpoint only makes the empty subflow, so add the requested executions into it
        client.addExecutions(realm(), alias, executions)

        if (requirement != null || priority != null) {
            client.updateFlowExecution(
                realm(),
                flowAlias,
                UpdateFlowExecution(
                    execution.id,
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

    // A subflow's alias is unique in the realm and is its execution's display name
    private fun findSubFlowExecution(): AuthenticationExecution? =
        client.flowExecutions(realm(), flowAlias).firstOrNull { it.authenticationFlow && it.displayName == alias }

    private fun subFlowExecution(): AuthenticationExecution =
        findSubFlowExecution() ?: throw MigrationException("SubFlow $alias was added but not found in $flowAlias!")

    /**
     * Deleting the subflow's execution also deletes the subflow and everything in it.
     *
     * The id may be unknown if execute() failed between creating the subflow and retrieving it, so fall
     * back to resolving the subflow's execution by its alias.
     */
    override fun undo() {
        val id = executionId ?: findSubFlowExecution()?.id ?: return
        client.deleteFlowExecution(realm(), id)
    }

    override fun name() = "AddSubFlow $alias to $flowAlias"
}
