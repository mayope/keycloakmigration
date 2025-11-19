package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import java.util.UUID

class DeleteFlowExecutionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteFlowExecution() {
        val alias = "TestFlow"
        val provider = "idp-auto-link"

        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(),
                    Flow.Requirement.REQUIRED,
                    provider,
                    0, 0, 0
                )
            )
        ).executeIt()

        DeleteFlowExecutionAction(testRealm, alias, provider).executeIt()

        val executions = client.flowExecutions(testRealm, alias)
        assertThat(executions).isEmpty()
    }

    @Test
    fun testDeleteFlowExecution_NotExisting() {
        val alias = "TestFlow2"
        val provider = "idp-auto-link"
        val nonExistentProvider = "non-existent-provider"

        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(),
                    Flow.Requirement.REQUIRED,
                    provider,
                    0, 0, 0
                )
            )
        ).executeIt()

        assertThatThrownBy {
            DeleteFlowExecutionAction(testRealm, alias, nonExistentProvider).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("No flow execution found with providerId: $nonExistentProvider")
    }

    @Test
    fun testUndoDeleteFlowExecution() {
        val alias = "TestFlow"
        val provider = "idp-auto-link"
        val execution = AuthenticationExecutionImport(
            UUID.randomUUID(),
            Flow.Requirement.REQUIRED,
            provider,
            0, 0, 0
        )

        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(execution)
        ).executeIt()

        val deleteAction = DeleteFlowExecutionAction(testRealm, alias, provider)
        deleteAction.executeIt()

        var executions = client.flowExecutions(testRealm, alias)
        assertThat(executions).isEmpty()

        deleteAction.undoIt()

        executions = client.flowExecutions(testRealm, alias)
        assertThat(executions).hasSize(1)
        assertThat(executions[0].providerId).isEqualTo(provider)
    }
}
