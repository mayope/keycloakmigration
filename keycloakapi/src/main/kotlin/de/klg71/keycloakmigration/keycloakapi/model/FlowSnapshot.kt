package de.klg71.keycloakmigration.keycloakapi.model

import java.util.UUID

/**
 * A flow's executions as a tree, as read before a change so the change can be undone. Unlike the flat
 * list Keycloak returns, it keeps sub flows with their settings and the executions nested in them.
 */
sealed interface FlowNode {
    val requirement: Flow.Requirement
    val priority: Int
}

data class ExecutionNode(
    val providerId: String?,
    override val requirement: Flow.Requirement,
    override val priority: Int,
    val config: AuthenticatorConfig? = null
) : FlowNode

data class SubFlowNode(
    val alias: String,
    val description: String,
    val type: String,
    val providerId: String?,
    override val requirement: Flow.Requirement,
    override val priority: Int,
    val executions: List<FlowNode>
) : FlowNode

/** The parts of a flow (top-level or sub flow) that a sub flow snapshot needs. */
data class FlowSummary(
    val id: UUID,
    val alias: String,
    val providerId: String,
    val description: String? = null
)

/**
 * What Keycloak returns in place of a config value it hides: a secret, or a key the provider doesn't
 * declare. The real value can't be read back.
 */
const val MASKED_CONFIG_VALUE = "**********"

/** Whether Keycloak returned any of this config's values masked, so the config can't be recreated. */
fun AuthenticatorConfig.hasMaskedValues() = MASKED_CONFIG_VALUE in config.values

/** The configs in this tree that can't be recreated, because Keycloak returned values of theirs masked. */
fun List<FlowNode>.maskedConfigs(): List<AuthenticatorConfig> = flatMap {
    when (it) {
        is ExecutionNode -> listOfNotNull(it.config?.takeIf { config -> config.hasMaskedValues() })
        is SubFlowNode -> it.executions.maskedConfigs()
    }
}
