package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.executionsToImport
import de.klg71.keycloakmigration.keycloakapi.importFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.ImportFlow

class DeleteFlowAction(
    realm: String?,
    private val alias: String,
) : Action(realm) {

    private var oldFlow: Flow? = null
    private val oldExecutions: MutableList<AuthenticationExecution> = mutableListOf()

    override fun execute() {
        client.flows(realm())
            .firstOrNull { it.alias == alias }?.let {
                oldFlow = it
                oldExecutions.addAll(client.flowExecutions(realm(), alias))
                client.deleteFlow(realm(), it.id)
            }
    }

    override fun undo() {
        oldFlow?.let {
            client.importFlow(
                realm(), ImportFlow(
                    alias, it.description, it.providerId, it.topLevel, it.builtIn,
                    executionsToImport(oldExecutions)
                )
            )
        }
    }

    override fun name() = "DeleteFlow $alias"
}

