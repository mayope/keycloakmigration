package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.copyAuthFlow
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import java.util.UUID

class CopyFlowAction(
  realm: String,
  private val flowAlias: String,
  private val newName: String
) : Action(realm) {

  override fun execute() {
    client.copyAuthFlow(realm(), flowAlias, newName)
  }

  override fun undo() {
    val flow = client.flows(realm()).first { flow: Flow -> flow.alias == newName }

    client.deleteFlow(realm(), flow.id)
  }

  override fun name() = "CopyFlow $flowAlias"
}