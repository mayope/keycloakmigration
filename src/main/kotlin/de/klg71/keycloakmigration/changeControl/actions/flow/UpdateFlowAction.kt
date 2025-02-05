package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.executionsToImport
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowInPlace
import de.klg71.keycloakmigration.keycloakapi.updateFlowInPlace


@Suppress("LongParameterList")
class UpdateFlowAction(
    realm: String?,
    private val alias: String,
    private val newAlias: String? = null,
    private val description: String? = null,
    private val providerId: String? = null,
    private val topLevel: Boolean? = null,
    private val executions: List<AuthenticationExecutionImport>? = null
) : Action(realm) {

    private var oldFlow: Flow? = null
    private val oldExecutions: MutableList<AuthenticationExecutionImport> = mutableListOf()

    override fun execute() {
        client.flows(realm())
            .firstOrNull { it.alias == alias }?.also {
                oldFlow = it
                oldExecutions.addAll(client.executionsToImport(realm(), alias))
            }?.let {
                client.updateFlowInPlace(
                    realm(), it.alias, UpdateFlowInPlace(
                        newAlias ?: it.alias,
                        description ?: it.description,
                        providerId ?: it.providerId,
                        topLevel ?: it.topLevel,
                        executions ?: oldExecutions
                    )
                )
            }
    }

    override fun undo() {
        val updateFlow = oldFlow ?: error("undo called but oldFlow is null")

        client.flows(realm())
            .firstOrNull { it.alias == newAlias ?: alias }?.let {
                client.updateFlowInPlace(
                    realm(), it.alias, UpdateFlowInPlace(
                        updateFlow.alias,
                        updateFlow.description,
                        updateFlow.providerId,
                        updateFlow.topLevel,
                        oldExecutions
                    )
                )
            }
    }

    override fun name() = "UpdateFlow $alias"
}
