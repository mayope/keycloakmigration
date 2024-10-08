package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.model.AddFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import java.util.*

class AddFlowExecutionAction(
  realm: String,
  private val flowAlias: String,
  private val provider: String,
  private val executionAlias: String,
  private val config: Map<String, String> = emptyMap()
) : Action(realm) {

  private lateinit var executionId: UUID

  override fun execute() {
    executionId = client.addFlowExecution(realm(), flowAlias, AddFlowExecution(provider)).extractLocationUUID()

    if (config.isNotEmpty())
      client.updateFlowExecutionWithNewConfiguration(
        realm(),
        executionId.toString(),
        AuthenticatorConfig(executionAlias, config)
      )
  }

  override fun undo() {
    client.deleteFlowExecution(realm(), executionId)
  }

  override fun name() = "AddFlowExecution [$provider] to $flowAlias"
}