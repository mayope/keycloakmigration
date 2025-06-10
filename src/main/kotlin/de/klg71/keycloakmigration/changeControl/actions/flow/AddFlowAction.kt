package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.importFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationFlowAction
import de.klg71.keycloakmigration.keycloakapi.model.ImportFlow
import java.util.UUID


@Suppress("LongParameterList")
class AddFlowAction(
    realm: String?,
    private val alias: String,
    private val description: String = "",
    private val buildIn: Boolean = false,
    private val providerId: String = "basic-flow",
    private val topLevel: Boolean = true,
    private val executions: List<AuthenticationFlowAction> = emptyList()
) : Action(realm) {

    private lateinit var flowUuid: UUID

    private fun addFlow() = ImportFlow(alias, description, providerId, topLevel, buildIn, executions)

    override fun execute() {
        flowUuid = client.importFlow(realm(), addFlow())
    }

    override fun undo() {
        client.deleteFlow(realm(), flowUuid)
    }

    override fun name() = "AddFlow $alias"
}
