package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.addExecutionConfig
import de.klg71.keycloakmigration.keycloakapi.addExecutionToFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import java.util.UUID

@Suppress("LongParameterList")
class AddFlowExecutionAction(
    realm: String?,
    private val flowAlias: String,
    private val provider: String,
    private val executionAlias: String,
    private val config: Map<String, String> = emptyMap(),
    private val requirement: Flow.Requirement? = null,
    private val priority: Int? = null
) : Action(realm) {

    private lateinit var executionId: UUID

    override fun execute() {
        // Marked executed as soon as the execution exists, so it is rolled back if a later call fails
        client.addExecutionToFlow(realm(), flowAlias, provider, requirement, priority) {
            executionId = it
            setExecuted()
        }

        // Checked: Keycloak refuses a config alias that is already taken, which used to leave the execution
        // silently without its config. The execution is rolled back instead.
        if (config.isNotEmpty())
            client.addExecutionConfig(realm(), executionId, AuthenticatorConfig(executionAlias, config))
    }

    override fun undo() {
        client.deleteFlowExecution(realm(), executionId)
    }

    override fun name() = "AddFlowExecution [$provider] to $flowAlias"
}
