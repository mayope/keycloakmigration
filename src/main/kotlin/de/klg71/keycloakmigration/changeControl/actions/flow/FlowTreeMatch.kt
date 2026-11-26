package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import de.klg71.keycloakmigration.keycloakapi.model.ExecutionNode
import de.klg71.keycloakmigration.keycloakapi.model.FlowNode
import de.klg71.keycloakmigration.keycloakapi.model.MASKED_CONFIG_VALUE
import de.klg71.keycloakmigration.keycloakapi.model.SubFlowNode

/**
 * Whether the [actual] tree read from the realm is the [desired] tree. Keycloak lists executions by
 * priority, so both are compared in that order.
 *
 * An empty config counts as no config: `addFlow` creates one for every execution. Values Keycloak
 * masks when they are read can't be compared, so they count as equal.
 */
internal fun flowTreeMatches(desired: List<FlowNode>, actual: List<FlowNode>): Boolean =
    desired.size == actual.size &&
        desired.sortedBy { it.priority }.zip(actual.sortedBy { it.priority }).all { (d, a) -> nodeMatches(d, a) }

private fun nodeMatches(desired: FlowNode, actual: FlowNode): Boolean = when {
    desired is ExecutionNode && actual is ExecutionNode -> executionMatches(desired, actual)
    desired is SubFlowNode && actual is SubFlowNode -> subFlowMatches(desired, actual)
    else -> false
}

private fun executionMatches(desired: ExecutionNode, actual: ExecutionNode) =
    desired.providerId == actual.providerId &&
        desired.requirement == actual.requirement &&
        desired.priority == actual.priority &&
        configMatches(desired.config, actual.config)

private fun subFlowMatches(desired: SubFlowNode, actual: SubFlowNode) =
    desired.alias == actual.alias &&
        desired.type == actual.type &&
        desired.description == actual.description &&
        (desired.type != "form-flow" || desired.providerId == actual.providerId) &&
        desired.requirement == actual.requirement &&
        desired.priority == actual.priority &&
        flowTreeMatches(desired.executions, actual.executions)

private fun configMatches(desired: AuthenticatorConfig?, actual: AuthenticatorConfig?): Boolean = when {
    desired == null -> actual == null || actual.config.isEmpty()
    actual == null -> false
    else -> actual.alias == desired.alias &&
        actual.config.keys == desired.config.keys &&
        desired.config.all { (key, value) -> actual.config[key] == value || actual.config[key] == MASKED_CONFIG_VALUE }
}
