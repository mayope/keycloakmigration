package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import de.klg71.keycloakmigration.keycloakapi.model.ExecutionNode
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.FlowNode
import de.klg71.keycloakmigration.keycloakapi.model.SubFlowNode

private const val PRIORITY_STEP = 10

/**
 * One entry of a `replaceFlow` tree. An entry with a [providerId] is an execution, an entry with an [alias]
 * is a sub flow with its own [executions].
 */
data class FlowEntry(
    val providerId: String? = null,
    val alias: String? = null,
    val requirement: Flow.Requirement,
    val priority: Int? = null,
    val config: Map<String, String>? = null,
    val configAlias: String? = null,
    val type: String = "basic-flow",
    val provider: String? = null,
    val description: String = "",
    val executions: List<FlowEntry> = emptyList()
)

/**
 * Turns the declared entries of the flow (or sub flow) [parentAlias] into the tree the flow should have.
 * Priorities default to the list order, config aliases to `<parent alias> <providerId>`.
 */
internal fun List<FlowEntry>.toFlowNodes(parentAlias: String): List<FlowNode> = mapIndexed { index, entry ->
    val priority = entry.priority ?: ((index + 1) * PRIORITY_STEP)
    if (entry.alias != null) {
        SubFlowNode(
            entry.alias, entry.description, entry.type, entry.provider, entry.requirement, priority,
            entry.executions.toFlowNodes(entry.alias)
        )
    } else {
        ExecutionNode(
            entry.providerId, entry.requirement, priority,
            // Keycloak drops empty values when it creates a config, so they aren't part of the tree
            entry.config?.let {
                val configAlias = entry.configAlias ?: "$parentAlias ${entry.providerId}"
                AuthenticatorConfig(configAlias, it.filterValues(String::isNotEmpty))
            }
        )
    }
}.sortedBy { it.priority }

/**
 * Checks the declared tree of the flow [flowAlias] before anything in the realm changes.
 */
internal fun validateFlowEntries(flowAlias: String, entries: List<FlowEntry>) {
    val nodes = entries.also { validateLevel(it) }.toFlowNodes(flowAlias)

    val subFlowAliases = nodes.subFlows().map { it.alias }
    (subFlowAliases + flowAlias).duplicates().firstOrNull()?.let {
        throw MigrationException("Flow alias $it is used more than once in the replaceFlow tree of $flowAlias")
    }
    nodes.configs().map { it.alias }.duplicates().firstOrNull()?.let {
        throw MigrationException(
            "Config alias $it is used more than once in the replaceFlow tree of $flowAlias, set configAlias"
        )
    }
}

private fun validateLevel(entries: List<FlowEntry>) {
    entries.forEach { entry ->
        entryProblem(entry)?.let { throw MigrationException(it) }
        validateLevel(entry.executions)
    }
    entries.mapIndexed { index, entry -> entry.priority ?: ((index + 1) * PRIORITY_STEP) }
        .duplicates().firstOrNull()?.let {
            throw MigrationException("Priority $it is used more than once in the same replaceFlow (sub) flow")
        }
}

private fun entryProblem(entry: FlowEntry): String? = when {
    (entry.providerId == null) == (entry.alias == null) ->
        "A replaceFlow entry needs either providerId (an execution) or alias (a sub flow): $entry"

    entry.alias != null && entry.type !in SUB_FLOW_TYPES ->
        "Sub flow ${entry.alias} has type ${entry.type}, it must be one of $SUB_FLOW_TYPES"

    entry.alias != null && (entry.config != null || entry.configAlias != null) ->
        "Sub flow ${entry.alias} can't have a config, only executions can"

    entry.providerId != null && entry.executions.isNotEmpty() ->
        "Execution ${entry.providerId} can't have executions, only sub flows can"

    entry.configAlias != null && entry.config == null ->
        "Execution ${entry.providerId} has a configAlias but no config"

    else -> null
}

internal fun List<FlowNode>.subFlows(): List<SubFlowNode> =
    filterIsInstance<SubFlowNode>().flatMap { listOf(it) + it.executions.subFlows() }

internal fun List<FlowNode>.configs(): List<AuthenticatorConfig> = flatMap {
    when (it) {
        is ExecutionNode -> listOfNotNull(it.config)
        is SubFlowNode -> it.executions.configs()
    }
}

private fun <T> List<T>.duplicates() = groupingBy { it }.eachCount().filterValues { it > 1 }.keys
