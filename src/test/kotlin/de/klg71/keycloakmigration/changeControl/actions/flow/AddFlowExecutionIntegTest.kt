package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddFlowExecutionIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddFlowExecution() {
        val alias = "TestFlow"
        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(
                    Flow.Requirement.REQUIRED,
                    "idp-auto-link",
                    0, 0
                )
            )
        ).executeIt()

        AddFlowExecutionAction(
            testRealm,
            flowAlias = alias,
            provider = "deny-access-authenticator",
            executionAlias = "Deny Access"
        ).executeIt()

        val flows = client.flows(testRealm)
        assertThat(flows.any { it.alias == alias }).isTrue
        val executions = client.flowExecutions(testRealm, alias)
        assertThat(executions).hasSize(2)
        assertThat(executions.first().providerId).isEqualTo("idp-auto-link")
        assertThat(executions[1].providerId).isEqualTo("deny-access-authenticator")
        assertThat(executions[1].requirement).isEqualTo(Flow.Requirement.DISABLED)
        assertThat(executions[1].index).isEqualTo(1)
        assertThat(executions[1].level).isEqualTo(0)
        assertThat(executions[1].authenticationConfig?.isEmpty())
    }

    @Test
    fun testAddFlowExecution_NotExisting() {
        assertThatThrownBy {
            AddFlowExecutionAction(
                testRealm,
                flowAlias = "TestFlow",
                provider = "deny-access-authenticator",
                executionAlias = "Deny Access"
            ).executeIt()
        }.isInstanceOf(KeycloakApiException::class.java).hasMessageContaining("Parent flow doesn't exist")
    }

}
