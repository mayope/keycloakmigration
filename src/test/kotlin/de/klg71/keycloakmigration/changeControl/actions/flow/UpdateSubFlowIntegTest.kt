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

class UpdateSubFlowIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testUpdateSubFlow() {
        val alias = "direct grant (copy)"
        val subFlowAlias = "$alias Direct Grant - Conditional OTP"

        CopyFlowAction(testRealm, flowAlias = "direct grant", newName = alias).executeIt()
        UpdateSubFlowAction(
            testRealm,
            subFlow = subFlowAlias,
            topLevelFlow = alias,
            requirement = Flow.Requirement.DISABLED
        ).executeIt()

        val flows = client.flows(testRealm)
        assertThat(flows.any { it.alias == alias }).isTrue
        val executions = client.flowExecutions(testRealm, alias)
        val subFlow = executions.first { it.displayName == subFlowAlias }
        assertThat(subFlow).isNotNull()
        assertThat(subFlow.requirement).isEqualTo(Flow.Requirement.DISABLED)
    }

    @Test
    fun testAddFlowExecution_SubFlowNotExisting() {
        val alias = "TestFlow"
        val subFlowAlias = "TestSubFlow"

        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(),
                    Flow.Requirement.REQUIRED,
                    "idp-auto-link",
                    0, 0, 0
                )
            )
        ).executeIt()

        assertThatThrownBy {
            UpdateSubFlowAction(
                testRealm,
                subFlow = subFlowAlias,
                topLevelFlow = alias,
                requirement = Flow.Requirement.DISABLED
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("SubFlow $subFlowAlias doesn't exist for $alias!")
    }

    @Test
    fun testAddFlowExecution_TopLevelFlowNotExisting() {
        val alias = "TestFlow"

        assertThatThrownBy {
            UpdateSubFlowAction(
                testRealm,
                subFlow = "TestSubFlow",
                topLevelFlow = alias,
                requirement = Flow.Requirement.DISABLED
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Flow $alias does not exist!")
    }
}
