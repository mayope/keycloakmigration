package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.createFlowFromSnapshot
import de.klg71.keycloakmigration.keycloakapi.flowBindings
import de.klg71.keycloakmigration.keycloakapi.flowSnapshot
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import de.klg71.keycloakmigration.keycloakapi.model.ExecutionNode
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.FlowBinding
import de.klg71.keycloakmigration.keycloakapi.model.FlowNode
import de.klg71.keycloakmigration.keycloakapi.model.maskedConfigs
import de.klg71.keycloakmigration.keycloakapi.model.SubFlowNode
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlow
import de.klg71.keycloakmigration.keycloakapi.moveFlowBinding
import java.util.UUID

private const val RUN_ID_LENGTH = 8
// Keycloak doesn't allow brackets in flow aliases
private val TEMPORARY_SUFFIX = Regex(""" replacing-[0-9a-f]{8}$""")

/**
 * Makes the top-level flow [alias] match the declared tree of [executions], sub flows included.
 *
 * If the flow already matches, nothing changes. Otherwise the new tree is built next to the old flow under
 * temporary aliases, everything that uses the old flow is moved to it, the old flow is deleted and the new
 * flow and its sub flows and configs get their final aliases. A login never sees a half-built flow.
 *
 * Undo puts the old flow back as it was, with everything that used it.
 */
@Suppress("TooManyFunctions")
class ReplaceFlowAction(
    realm: String?,
    private val alias: String,
    private val description: String = "",
    private val providerId: String = "basic-flow",
    private val executions: List<FlowEntry> = emptyList()
) : Action(realm) {

    private val suffix = " replacing-${UUID.randomUUID().toString().take(RUN_ID_LENGTH)}"

    private var oldFlow: Flow? = null
    private var oldExecutions: List<FlowNode> = emptyList()
    private var oldDeleted = false
    private var newFlowId: UUID? = null
    private val movedBindings = mutableListOf<FlowBinding>()

    private val desired by lazy { executions.toFlowNodes(alias) }

    override fun execute() {
        validateFlowEntries(alias, executions)
        val existing = handleLeftOverTemporaryFlows(client.flows(realm()).firstOrNull { it.alias == alias })

        if (existing == null) {
            refuseTakenAliases(null)
            setExecuted()
            build(alias, desired)
            return
        }

        if (existing.builtIn) throw MigrationException("Flow $alias is built in, replaceFlow can't replace it")

        val snapshot = client.flowSnapshot(realm(), alias)
        if (existing.description == description && existing.providerId == providerId &&
            flowTreeMatches(desired, snapshot)
        ) {
            LOG.info("Flow $alias already matches, nothing to replace")
            return
        }

        // Checked before anything changes: undo rebuilds the old flow from this snapshot
        refuseMaskedConfigs(name(), snapshot.maskedConfigs())
        refuseTakenAliases(existing)
        oldFlow = existing
        oldExecutions = snapshot
        // From here on the realm changes in several steps, so undo has to run if one of them fails
        setExecuted()

        val temporaryAlias = alias + suffix
        val flowId = build(temporaryAlias, desired.withSuffix())

        client.flowBindings(realm(), alias, existing.id).forEach {
            client.moveFlowBinding(realm(), it, temporaryAlias, flowId)
            movedBindings += it
        }

        client.deleteFlow(realm(), existing.id)
        oldDeleted = true

        renameTree(flowId, toTemporary = false)
    }

    /** Builds the top-level flow [flowAlias] with [nodes] and checks it came out as declared. */
    private fun build(flowAlias: String, nodes: List<FlowNode>): UUID {
        // Recorded as soon as the flow exists, so undo deletes it if filling it fails
        val flowId = client.createFlowFromSnapshot(
            realm(), AddFlow(flowAlias, false, description, providerId, true), nodes
        ) { newFlowId = it }

        if (!flowTreeMatches(nodes, client.flowSnapshot(realm(), flowAlias))) {
            throw MigrationException("Flow $flowAlias doesn't match its declared tree after building it")
        }
        return flowId
    }

    /**
     * Deals with flows left over from an earlier run that was stopped without a rollback, and returns the
     * flow to replace.
     *
     * A left-over flow nothing uses is deleted. One that is in use while no flow has the final alias is
     * from a run stopped after it deleted the old flow: its rename is finished, and it becomes the flow
     * to replace. That repair isn't undone on rollback. Any other left-over flow in use needs a person to
     * look at it.
     */
    private fun handleLeftOverTemporaryFlows(existing: Flow?): Flow? {
        var flow = existing
        client.flows(realm())
            .filter { it.alias.startsWith(alias) && TEMPORARY_SUFFIX.matches(it.alias.removePrefix(alias)) }
            .forEach {
                when {
                    client.flowBindings(realm(), it.alias, it.id).isEmpty() -> {
                        LOG.warn("Deleting flow ${it.alias}, left over from an earlier replaceFlow of $alias")
                        client.deleteFlow(realm(), it.id)
                    }

                    flow == null -> {
                        LOG.warn("Finishing the rename of flow ${it.alias}, left over from an earlier replaceFlow")
                        renameTree(it.id, toTemporary = false)
                        flow = client.flows(realm()).first { adopted -> adopted.id == it.id }
                    }

                    else -> throw MigrationException(
                        "Flow ${it.alias} is left over from an earlier replaceFlow of $alias and is still in use. " +
                            "Move what uses it to the right flow, then run the migration again."
                    )
                }
            }
        return flow
    }

    /**
     * Refuses a tree whose sub flow or config aliases another flow already uses. Keycloak keeps both
     * unique in the realm, so the final rename would fail only after the old flow is gone.
     */
    private fun refuseTakenAliases(existing: Flow?) {
        takenAlias(existing)?.let {
            throw MigrationException("$it in the replaceFlow tree of $alias is already used elsewhere in the realm")
        }
    }

    private fun takenAlias(existing: Flow?): String? {
        val subFlowAliases = desired.subFlows().map { it.alias }.toSet()
        val configAliases = desired.configs().mapNotNull { it.alias }.toSet()

        return client.flows(realm()).asSequence()
            .filter { it.id != existing?.id }
            .flatMap { flow ->
                val topLevel = "Sub flow alias '${flow.alias}' (a top-level flow)"
                    .takeIf { flow.alias in subFlowAliases }
                sequenceOf(topLevel) +
                    client.flowExecutions(realm(), flow.alias).asSequence()
                        .map { usedAlias(it, flow.alias, subFlowAliases, configAliases) }
            }
            .filterNotNull()
            .firstOrNull()
    }

    private fun usedAlias(
        execution: AuthenticationExecution,
        flowAlias: String,
        subFlowAliases: Set<String>,
        configAliases: Set<String>
    ): String? = if (execution.authenticationFlow) {
        "Sub flow alias '${execution.displayName}' (in flow $flowAlias)"
            .takeIf { execution.displayName in subFlowAliases }
    } else {
        execution.authenticationConfig
            ?.let { client.getAuthenticatorConfiguration(realm(), it).alias }
            ?.takeIf { it in configAliases }
            ?.let { "Config alias '$it' (in flow $flowAlias)" }
    }

    private fun List<FlowNode>.withSuffix(): List<FlowNode> = map {
        when (it) {
            is ExecutionNode -> it.copy(config = it.config?.let { c -> c.copy(alias = c.alias + suffix) })
            is SubFlowNode -> it.copy(alias = it.alias + suffix, executions = it.executions.withSuffix())
        }
    }

    private fun target(name: String, toTemporary: Boolean) = when {
        !toTemporary -> name.replace(TEMPORARY_SUFFIX, "")
        TEMPORARY_SUFFIX.containsMatchIn(name) -> name
        else -> name + suffix
    }

    /**
     * Renames the flow [flowId], its sub flows and its configs to their final aliases, or back to the
     * temporary ones. Names already right are left alone, so this also finishes a partial rename.
     */
    private fun renameTree(flowId: UUID, toTemporary: Boolean) {
        val flow = client.flowById(realm(), flowId)
        val flowAlias = target(flow.alias, toTemporary)
        if (flowAlias != flow.alias) {
            client.updateFlow(
                realm(), flowId, UpdateFlow(flowId, flowAlias, flow.providerId, true, flow.description ?: "")
            )
        }

        // Config values are read back masked when Keycloak treats them as secret, so write the declared ones
        val declaredConfigs = desired.configs().associate { it.alias to it.config }

        client.flowExecutions(realm(), flowAlias).forEach { execution ->
            if (execution.authenticationFlow) {
                val subFlowId = execution.flowId
                    ?: throw MigrationException("Sub flow ${execution.displayName} has no flow id")
                val subFlow = client.flowById(realm(), subFlowId)
                val subFlowAlias = target(subFlow.alias, toTemporary)
                if (subFlowAlias != subFlow.alias) {
                    client.updateFlow(
                        realm(), subFlowId,
                        UpdateFlow(subFlowId, subFlowAlias, subFlow.providerId, false, subFlow.description ?: "")
                    )
                }
            } else {
                execution.authenticationConfig?.let { renameConfig(it, toTemporary, declaredConfigs) }
            }
        }
    }

    private fun renameConfig(
        configId: String,
        toTemporary: Boolean,
        declaredConfigs: Map<String?, Map<String, String>>
    ) {
        val config = client.getAuthenticatorConfiguration(realm(), configId)
        val currentAlias = config.alias ?: return
        val configAlias = target(currentAlias, toTemporary)
        if (configAlias == currentAlias) return

        // Values Keycloak returned masked can be sent back as they are: the update keeps the stored value
        // for a masked one. Keycloak doesn't check that the new alias is free here, which takenAlias does
        // beforehand for every config used by a flow. A config no execution uses can't be checked, as
        // Keycloak has no call to list a realm's configs.
        val values = declaredConfigs[target(currentAlias, toTemporary = false)] ?: config.config
        client.updateAuthenticatorConfiguration(
            realm(), configId, AuthenticatorConfig(configAlias, values, UUID.fromString(configId))
        ).run {
            if (!isSuccessful()) {
                throw MigrationException(
                    "Failed to rename authenticator config $currentAlias to $configAlias. " +
                        "Status: ${status()}, Reason: ${reason()}"
                )
            }
        }
    }

    override fun undo() {
        val flowId = newFlowId
        val newFlowExists = flowId != null && client.flows(realm()).any { it.id == flowId }
        val old = oldFlow

        if (old == null) {
            // The flow didn't exist before, so it was built under its final alias
            if (newFlowExists) client.deleteFlow(realm(), flowId!!)
            return
        }

        // Free the final aliases before the old flow takes them again
        if (newFlowExists) renameTree(flowId!!, toTemporary = true)

        val oldFlowId = if (oldDeleted) {
            client.createFlowFromSnapshot(
                realm(), AddFlow(old.alias, old.builtIn, old.description, old.providerId, old.topLevel), oldExecutions
            )
        } else {
            old.id
        }

        movedBindings.forEach { client.moveFlowBinding(realm(), it, old.alias, oldFlowId) }

        if (newFlowExists) client.deleteFlow(realm(), flowId!!)
    }

    override fun name() = "ReplaceFlow $alias"
}
