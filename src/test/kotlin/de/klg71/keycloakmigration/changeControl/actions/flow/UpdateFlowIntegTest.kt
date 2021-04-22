package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject

class UpdateFlowIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testUpdateFlow() {
        val alias = "FloRida"
        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(Flow.Requirement.REQUIRED, "idp-auto-link", 0, 0)
            )
        ).executeIt()

        val flow = client.flows(testRealm).first { it.alias == alias }
        val newAlias = "FloRider"
        val description = "Left round"
        UpdateFlowAction(
            testRealm, flow.alias, newAlias, description, null, null, listOf(
                AuthenticationExecutionImport(Flow.Requirement.REQUIRED, "idp-confirm-link", 0, 0)
            )
        ).executeIt()
        val updatedFlow = client.flows(testRealm).first { it.alias == newAlias }
        val executions = client.flowExecutions(testRealm, newAlias)
        assertThat(updatedFlow.description).isEqualTo(description)
        assertThat(executions).hasSize(1)
        assertThat(executions.first().providerId).isEqualTo("idp-confirm-link")
        assertThat(executions.first().requirement).isEqualTo(Flow.Requirement.REQUIRED)
    }

    @Test
    fun testUpdateFlow_Rollback() {
        val alias = "FloRida"
        val originalDescription = "Right round"
        AddFlowAction(
            testRealm, alias, originalDescription, executions = listOf(
                AuthenticationExecutionImport(Flow.Requirement.REQUIRED, "idp-auto-link", 0, 0)
            )
        ).executeIt()

        val flow = client.flows(testRealm).first { it.alias == alias }
        val newAlias = "FloRider"
        val description = "Left round"
        val action = UpdateFlowAction(
            testRealm, flow.alias, newAlias, description, null, null, listOf(
                AuthenticationExecutionImport(Flow.Requirement.REQUIRED, "idp-confirm-link", 0, 0)
            )
        )
        action.executeIt()
        val updatedFlow = client.flows(testRealm).first { it.alias == newAlias }
        val executions = client.flowExecutions(testRealm, newAlias)
        assertThat(updatedFlow.description).isEqualTo(description)
        action.undoIt()
        val oldFlow = client.flows(testRealm).first { it.alias == alias }
        assertThat(oldFlow.description).isEqualTo(originalDescription)
    }

}
