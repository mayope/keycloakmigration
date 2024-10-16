package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionInfo
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowExecution

class UpdateSubFlowAction(
  realm: String?,
  private val subFlow: String,
  private val topLevelFlow: String,
  private val requirement: Flow.Requirement
) : Action(realm) {

  private lateinit var original: AuthenticationExecutionInfo

  override fun execute() {

    original = client.flowExecutions(realm(), topLevelFlow)
      .first { it.displayName == subFlow && it.authenticationFlow}

    val updateFlowExecution = UpdateFlowExecution(
      original.id,
      requirement,
      original.level,
      original.index,
      original.providerId
    )

    client.updateFlowExecution(
      realm(),
      topLevelFlow,
      updateFlowExecution
    )
  }

  override fun undo() {
    val updateFlowExecution = UpdateFlowExecution(
      original.id,
      original.requirement,
      original.level,
      original.index,
      original.providerId
    )

    client.updateFlowExecution(realm(), topLevelFlow, updateFlowExecution)
  }

  override fun name() = "UpdateSubFlow $subFlow to $requirement"
}