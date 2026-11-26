@file:Suppress("TooManyFunctions")

package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.AddFlow
import de.klg71.keycloakmigration.keycloakapi.model.AddFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.AddSubFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationFlowAction
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import de.klg71.keycloakmigration.keycloakapi.model.CopyFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.ExecutionNode
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.FlowNode
import de.klg71.keycloakmigration.keycloakapi.model.ImportFlow
import de.klg71.keycloakmigration.keycloakapi.model.SubFlow
import de.klg71.keycloakmigration.keycloakapi.model.SubFlowNode
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowInPlace
import java.util.*

fun KeycloakClient.importFlow(realm: String, importFlow: ImportFlow): UUID {
    if (flows(realm).any { it.alias == importFlow.alias }) {
        throw KeycloakApiException("Import Flow failed, Flow: ${importFlow.alias} already exists")
    }
    return createFlow(realm, importFlow).also {
        addExecutions(realm, importFlow.alias, importFlow.authenticationExecutions)
    }
}

private fun KeycloakClient.configureSubFlow(
    realm: String,
    flowAlias: String,
    subflow: AddSubFlow,
    executions: List<AuthenticationFlowAction>
) {
    addSubFlowToFlow(realm, flowAlias, subflow)
    addExecutions(realm, subflow.alias, executions)
}

/**
 * Adds a list of [executions] into the flow (or subflow) identified by [flowAlias]. Plain executions
 * are added directly; nested subflows are configured recursively. This is the single place that maps
 * an [AuthenticationFlowAction] to the call that materializes it.
 */
fun KeycloakClient.addExecutions(
    realm: String,
    flowAlias: String,
    executions: List<AuthenticationFlowAction>
) {
    executions.forEach {
        when (it) {
            is AuthenticationExecutionImport -> addExecution(realm, flowAlias, it)
            // The create endpoint reads "provider", so map SubFlow.providerId onto AddSubFlow.provider.
            is SubFlow -> configureSubFlow(
                realm, flowAlias,
                AddSubFlow(it.alias, it.description, it.type, it.providerId),
                it.authenticationExecutions
            )
        }
    }
}

fun KeycloakClient.copyAuthFlow(realm: String, flowAlias: String, newName: String) {
    val flows = flows(realm)

    if (flows.none { it.alias == flowAlias }) {
        throw KeycloakApiException("Copy Flow failed, Flow: $flowAlias does not exist")
    }

    if (flows.any { it.alias == newName }) {
        throw KeycloakApiException("Copy Flow failed, Flow: $newName already exists")
    }

    copyFlow(realm, flowAlias, CopyFlowExecution(newName))
}

fun KeycloakClient.updateFlowInPlace(realm: String, alias: String, updateFlow: UpdateFlowInPlace) {
    if (flows(realm).none { it.alias == alias }) {
        throw KeycloakApiException("Update Flow failed, Flow: $alias does not exist")
    }
    val flow = flows(realm).first { it.alias == alias }

    updateFlow(
        realm, flow.id,
        UpdateFlow(flow.id, updateFlow.newAlias, updateFlow.providerId, updateFlow.topLevel, updateFlow.description)
    )
    replaceFlowExecutions(realm, updateFlow.newAlias, updateFlow.authenticationExecutions)
}

private fun KeycloakClient.createFlow(realm: String, importFlow: ImportFlow): UUID {
    return addFlow(
        realm, AddFlow(
            importFlow.alias, importFlow.buildIn, importFlow.description, importFlow.providerId, importFlow.topLevel
        )
    ).extractLocationUUID()
}

private fun KeycloakClient.addExecution(
    realm: String,
    flowAlias: String,
    executionImport: AuthenticationExecutionImport
) {
    // addFlow and updateFlow have never checked these two responses, and keep not checking them: a
    // requirement or priority Keycloak rejects is left at its default, and the config, named after the
    // provider and created even when empty, is missing when another config already has that alias
    val executionId =
        addFlowExecution(realm, flowAlias, AddFlowExecution(executionImport.providerId)).extractLocationUUID()
    updateFlowExecution(
        realm, flowAlias, UpdateFlowExecution(
            executionId, executionImport.requirement, executionImport.level, executionImport.index,
            executionImport.priority, executionImport.providerId
        )
    )
    updateFlowExecutionWithNewConfiguration(
        realm, executionId.toString(), AuthenticatorConfig(
            executionImport.providerId + "_config",
            executionImport.config,
            null
        )
    )
}

/**
 * Replaces all executions of the flow [flowAlias] with [executions].
 */
fun KeycloakClient.replaceFlowExecutions(
    realm: String,
    flowAlias: String,
    executions: List<AuthenticationExecutionImport>
) {
    clearFlowExecutions(realm, flowAlias)
    executions.forEach {
        addExecution(realm, flowAlias, it)
    }
}

/**
 * Deletes all executions of the flow [flowAlias]. Only the top level is deleted: deleting a sub flow's
 * execution also deletes the sub flow and everything in it, and the executions list includes those
 * nested executions too.
 */
fun KeycloakClient.clearFlowExecutions(realm: String, flowAlias: String) {
    flowExecutions(realm, flowAlias).filter { it.level == 0 }.forEach {
        deleteFlowExecution(realm, it.id)
    }
}

/**
 * Reads the executions of the flow [flowAlias] as a tree, so they can be put back with
 * [restoreFlowSnapshot]. Sub flows keep their settings and nested executions, executions keep their
 * authenticator config.
 */
fun KeycloakClient.flowSnapshot(realm: String, flowAlias: String): List<FlowNode> {
    val executions = flowExecutions(realm, flowAlias)
    var position = 0

    // The executions list is in depth-first order: a sub flow is followed by its own executions one
    // level deeper, then the list continues with the sub flow's siblings.
    fun nodesAt(level: Int): List<FlowNode> {
        val nodes = mutableListOf<FlowNode>()
        while (position < executions.size && executions[position].level == level) {
            val execution = executions[position++]
            nodes += if (execution.authenticationFlow) {
                subFlowNode(realm, execution, nodesAt(level + 1))
            } else {
                ExecutionNode(
                    execution.providerId, execution.requirement, execution.priority,
                    execution.authenticationConfig?.let { getAuthenticatorConfiguration(realm, it) }
                )
            }
        }
        return nodes
    }

    return nodesAt(0).also {
        if (position != executions.size) {
            throw KeycloakApiException("Unexpected execution levels in flow $flowAlias, can't take a snapshot")
        }
    }
}

private fun KeycloakClient.subFlowNode(
    realm: String,
    execution: AuthenticationExecution,
    children: List<FlowNode>
): SubFlowNode {
    val flowId = execution.flowId
        ?: throw KeycloakApiException("Sub flow ${execution.displayName} has no flow id, can't take a snapshot")
    val subFlow = flowById(realm, flowId)
    return SubFlowNode(
        subFlow.alias, subFlow.description ?: execution.description ?: "", subFlow.providerId,
        execution.providerId, execution.requirement, execution.priority, children
    )
}

/**
 * Adds the executions of a [flowSnapshot] to the flow [flowAlias], recreating sub flows with their
 * requirement and priority and executions with their authenticator config.
 */
fun KeycloakClient.restoreFlowSnapshot(realm: String, flowAlias: String, nodes: List<FlowNode>) {
    nodes.forEach { node ->
        when (node) {
            is ExecutionNode -> {
                val executionId = addExecutionToFlow(realm, flowAlias, node.providerId, node.requirement, node.priority)
                node.config?.let { addExecutionConfig(realm, executionId, it) }
            }

            is SubFlowNode -> {
                addSubFlowToFlow(
                    realm, flowAlias, AddSubFlow(node.alias, node.description, node.type, node.providerId),
                    node.requirement, node.priority
                )
                restoreFlowSnapshot(realm, node.alias, node.executions)
            }
        }
    }
}

/**
 * Creates the top-level flow [flow] and fills it with the executions of a [flowSnapshot]. Returns its id.
 * [onCreated] gets the id as soon as the flow exists, before it is filled.
 */
fun KeycloakClient.createFlowFromSnapshot(
    realm: String,
    flow: AddFlow,
    nodes: List<FlowNode>,
    onCreated: (UUID) -> Unit = {}
): UUID = addFlow(realm, flow).extractLocationUUID().also {
    onCreated(it)
    restoreFlowSnapshot(realm, flow.alias, nodes)
}

/**
 * Adds an execution of [providerId] to the flow (or sub flow) [flowAlias] and returns its id. A
 * [requirement] or [priority] that is given replaces Keycloak's default for a new execution.
 * [onCreated] gets the id as soon as the execution exists, before the call that can still fail after it.
 */
@Suppress("LongParameterList")
fun KeycloakClient.addExecutionToFlow(
    realm: String,
    flowAlias: String,
    providerId: String?,
    requirement: Flow.Requirement? = null,
    priority: Int? = null,
    onCreated: (UUID) -> Unit = {}
): UUID {
    val executionId = addFlowExecution(realm, flowAlias, AddFlowExecution(providerId)).extractLocationUUID()
    onCreated(executionId)
    setRequirementAndPriority(realm, flowAlias, executionId, requirement, priority)
    return executionId
}

/**
 * Adds the sub flow [subFlow] to the flow (or sub flow) [flowAlias] and returns the sub flow's execution
 * in it. A [requirement] or [priority] that is given replaces Keycloak's default for a new sub flow.
 * [onCreated] runs as soon as the sub flow exists, before the calls that can still fail after it.
 */
@Suppress("LongParameterList")
fun KeycloakClient.addSubFlowToFlow(
    realm: String,
    flowAlias: String,
    subFlow: AddSubFlow,
    requirement: Flow.Requirement? = null,
    priority: Int? = null,
    onCreated: () -> Unit = {}
): AuthenticationExecution {
    addSubFlow(realm, flowAlias, subFlow).run {
        if (!isSuccessful()) {
            throw KeycloakApiException(
                "Failed to add subflow ${subFlow.alias} to $flowAlias: ${body()?.asReader()?.readText()}",
                statusCode = status()
            )
        }
    }
    onCreated()
    val execution = findSubFlowExecution(realm, flowAlias, subFlow.alias)
        ?: throw KeycloakApiException("Sub flow ${subFlow.alias} was added but not found in $flowAlias")
    setRequirementAndPriority(realm, flowAlias, execution.id, requirement, priority)
    return execution
}

/** The execution of the sub flow [alias] in [flowAlias]. A sub flow's alias is its execution's display name. */
fun KeycloakClient.findSubFlowExecution(realm: String, flowAlias: String, alias: String): AuthenticationExecution? =
    flowExecutions(realm, flowAlias).firstOrNull { it.authenticationFlow && it.displayName == alias }

/**
 * Creates the authenticator config of the execution [executionId] with the alias and values of [config].
 */
fun KeycloakClient.addExecutionConfig(realm: String, executionId: UUID, config: AuthenticatorConfig) {
    updateFlowExecutionWithNewConfiguration(
        realm, executionId.toString(), AuthenticatorConfig(config.alias, config.config)
    ).run {
        if (!isSuccessful()) {
            throw KeycloakApiException(
                "Failed to create authenticator config ${config.alias}: ${body()?.asReader()?.readText()}",
                statusCode = status()
            )
        }
    }
}

private fun KeycloakClient.setRequirementAndPriority(
    realm: String,
    flowAlias: String,
    executionId: UUID,
    requirement: Flow.Requirement?,
    priority: Int?
) {
    if (requirement == null && priority == null) return
    val execution = flowExecutions(realm, flowAlias).firstOrNull { it.id == executionId }
        ?: throw KeycloakApiException("Execution $executionId was added but not found in $flowAlias")
    updateFlowExecution(
        realm, flowAlias, UpdateFlowExecution(
            executionId, requirement ?: execution.requirement, execution.level, execution.index,
            priority ?: execution.priority, execution.providerId
        )
    ).run {
        if (!isSuccessful()) {
            val name = execution.providerId ?: execution.displayName
            throw KeycloakApiException(
                "Failed to set requirement/priority of $name in $flowAlias: ${body()?.asReader()?.readText()}",
                statusCode = status()
            )
        }
    }
}

fun KeycloakClient.executionsToImport(realm: String, flowAlias: String) = flowExecutions(realm, flowAlias).map {
    AuthenticationExecutionImport(
        it.id, it.requirement, it.providerId, it.level, it.index, it.priority,
        if (it.authenticationConfig == null) mapOf() else getAuthenticatorConfiguration(
            realm,
            it.authenticationConfig
        ).config
    )
}
