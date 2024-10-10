package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowExecution

class UpdateFlowExecutionAction(
    realm: String?,
    private val flowAlias: String,
    private val executionDisplayName: String,
    private val requirement: Flow.Requirement
) : Action(realm) {

    private lateinit var originalExecution: AuthenticationExecution

    override fun execute() {
        originalExecution = client.flowExecutions(realm(), flowAlias).first { authenticationExecution -> authenticationExecution.displayName == executionDisplayName }

        val updateFlowExecution = UpdateFlowExecution(
            originalExecution.id,
            requirement,
            originalExecution.level,
            originalExecution.index,
            originalExecution.providerId
        )

        client.updateFlowExecution(realm(), flowAlias, updateFlowExecution)
    }

    override fun undo() {
        val updateFlowExecution = UpdateFlowExecution(
            originalExecution.id,
            originalExecution.requirement,
            originalExecution.level,
            originalExecution.index,
            originalExecution.providerId
        )

        client.updateFlowExecution(realm(), flowAlias, updateFlowExecution)
    }

    override fun name() = "UpdateFlowExecution [$flowAlias] $executionDisplayName to $requirement"
}