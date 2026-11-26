package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.addExecutionConfig
import de.klg71.keycloakmigration.keycloakapi.addExecutionToFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.hasMaskedValues

/**
 * Deletes the first execution with [provider] from the flow [flowAlias], at any depth.
 *
 * Undo adds just that execution back into the flow or subflow it was deleted from, with its
 * requirement, priority and authenticator config. The rest of the flow is left alone.
 */
class DeleteFlowExecutionAction(
    realm: String?,
    private val flowAlias: String,
    private val provider: String,
) : Action(realm) {

    private class DeletedExecution(
        val parentAlias: String,
        val requirement: Flow.Requirement,
        val priority: Int,
        val config: AuthenticatorConfig?
    )

    private var deleted: DeletedExecution? = null

    override fun execute() {
        if (client.flows(realm()).none { it.alias == flowAlias }) return

        val executions = client.flowExecutions(realm(), flowAlias)
        val position = executions.indexOfFirst { it.providerId == provider }
        if (position < 0) throw MigrationException("No flow execution found with providerId: $provider")

        val execution = executions[position]
        if (execution.authenticationFlow) {
            throw MigrationException(
                "Flow execution with providerId: $provider is a subflow, deleteFlowExecution only deletes executions"
            )
        }

        val config = execution.authenticationConfig?.let { client.getAuthenticatorConfiguration(realm(), it) }
        refuseMaskedConfigs(name(), listOfNotNull(config?.takeIf { it.hasMaskedValues() }))

        val removed = DeletedExecution(
            parentAlias(executions, position), execution.requirement, execution.priority, config
        )
        client.deleteFlowExecution(realm(), execution.id)
        deleted = removed
    }

    /**
     * The executions list is in depth-first order, so the parent of an execution at level n > 0 is
     * the closest earlier subflow at level n - 1. Its display name is the subflow's alias.
     */
    private fun parentAlias(executions: List<AuthenticationExecution>, position: Int): String {
        val level = executions[position].level
        if (level == 0) return flowAlias
        return executions.subList(0, position)
            .lastOrNull { it.level == level - 1 && it.authenticationFlow }
            ?.displayName
            ?: throw MigrationException("No parent subflow found for flow execution with providerId: $provider")
    }

    override fun undo() {
        val execution = deleted ?: return
        val executionId = client.addExecutionToFlow(
            realm(), execution.parentAlias, provider, execution.requirement, execution.priority
        )
        execution.config?.let { client.addExecutionConfig(realm(), executionId, it) }
        deleted = null
    }

    override fun name() = "DeleteFlowExecution [$provider] to $flowAlias"
}
