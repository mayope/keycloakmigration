package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.addExecutions
import de.klg71.keycloakmigration.keycloakapi.addSubFlowToFlow
import de.klg71.keycloakmigration.keycloakapi.findSubFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.AddSubFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationFlowAction
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import feign.FeignException
import java.util.UUID

internal val SUB_FLOW_TYPES = setOf("basic-flow", "form-flow")

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

        val execution = try {
            // Marked executed as soon as the subflow exists, so it is rolled back if a later call fails.
            // Not before: if the alias is taken, undo would find and delete the subflow that holds it.
            client.addSubFlowToFlow(
                realm(), flowAlias, AddSubFlow(alias, description, type, provider), requirement, priority
            ) { setExecuted() }
        } catch (e: KeycloakApiException) {
            throw MigrationException("${e.message} (realm: ${realm()})", e)
        }
        executionId = execution.id

        // The create endpoint only makes the empty subflow, so add the requested executions into it
        client.addExecutions(realm(), alias, executions)
    }

    private fun findSubFlowExecution(): AuthenticationExecution? =
        client.findSubFlowExecution(realm(), flowAlias, alias)

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
