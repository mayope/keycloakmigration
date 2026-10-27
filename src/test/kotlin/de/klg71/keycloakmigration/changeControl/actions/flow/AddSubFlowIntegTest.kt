package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import feign.FeignException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddSubFlowIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    private val topLevelFlow = "SubFlowParent"

    private fun addTopLevelFlow() = AddFlowAction(testRealm, topLevelFlow).executeIt()

    private fun execution(flow: String, displayName: String) =
        client.flowExecutions(testRealm, flow).first { it.displayName == displayName }

    @Test
    fun testAddSubFlow() {
        addTopLevelFlow()

        AddSubFlowAction(
            testRealm, topLevelFlow, "Sub", description = "a subflow",
            requirement = Flow.Requirement.REQUIRED, priority = 30
        ).executeIt()

        val subFlow = execution(topLevelFlow, "Sub")
        assertThat(subFlow.authenticationFlow).isTrue
        assertThat(subFlow.requirement).isEqualTo(Flow.Requirement.REQUIRED)
        assertThat(subFlow.priority).isEqualTo(30)
        assertThat(subFlow.level).isEqualTo(0)
    }

    @Test
    fun testAddSubFlowDefaultsToDisabled() {
        addTopLevelFlow()

        AddSubFlowAction(testRealm, topLevelFlow, "Sub").executeIt()

        assertThat(execution(topLevelFlow, "Sub").requirement).isEqualTo(Flow.Requirement.DISABLED)
    }

    @Test
    fun testAddSubFlowToSubFlow() {
        addTopLevelFlow()
        AddSubFlowAction(testRealm, topLevelFlow, "Sub", requirement = Flow.Requirement.REQUIRED).executeIt()

        AddSubFlowAction(testRealm, "Sub", "Nested", requirement = Flow.Requirement.CONDITIONAL).executeIt()

        val nested = execution(topLevelFlow, "Nested")
        assertThat(nested.authenticationFlow).isTrue
        assertThat(nested.requirement).isEqualTo(Flow.Requirement.CONDITIONAL)
        assertThat(nested.level).isEqualTo(1)
        assertThat(client.flowExecutions(testRealm, "Sub").map { it.displayName }).containsExactly("Nested")
    }

    @Test
    fun testAddExecutionToNewSubFlow() {
        addTopLevelFlow()
        AddSubFlowAction(testRealm, topLevelFlow, "Sub", requirement = Flow.Requirement.REQUIRED).executeIt()

        AddFlowExecutionAction(
            testRealm, "Sub", "auth-cookie", "cookie", requirement = Flow.Requirement.ALTERNATIVE
        ).executeIt()

        val cookie = client.flowExecutions(testRealm, "Sub").single()
        assertThat(cookie.providerId).isEqualTo("auth-cookie")
        assertThat(cookie.requirement).isEqualTo(Flow.Requirement.ALTERNATIVE)
    }

    @Test
    fun testAddFormSubFlow() {
        addTopLevelFlow()

        AddSubFlowAction(
            testRealm, topLevelFlow, "Form", type = "form-flow", provider = "registration-page-form",
            requirement = Flow.Requirement.REQUIRED
        ).executeIt()

        val form = execution(topLevelFlow, "Form")
        assertThat(form.authenticationFlow).isTrue
        assertThat(form.providerId).isEqualTo("registration-page-form")
    }

    @Test
    fun testAddSubFlowParentMissing() {
        assertThatThrownBy { AddSubFlowAction(testRealm, "MissingFlow", "Sub").executeIt() }
            .isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("Flow MissingFlow does not exist")
    }

    @Test
    fun testAddSubFlowAliasExists() {
        addTopLevelFlow()

        assertThatThrownBy { AddSubFlowAction(testRealm, topLevelFlow, topLevelFlow).executeIt() }
            .isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("Failed to add subflow")
    }

    @Test
    fun testAddSubFlowInvalidType() {
        addTopLevelFlow()

        assertThatThrownBy { AddSubFlowAction(testRealm, topLevelFlow, "Sub", type = "client-flow").executeIt() }
            .isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("SubFlow type must be one of")
    }

    @Test
    fun testUndoAddSubFlowRemovesTheSubtree() {
        addTopLevelFlow()
        val action = AddSubFlowAction(testRealm, topLevelFlow, "Sub", requirement = Flow.Requirement.REQUIRED)
        action.executeIt()
        AddSubFlowAction(testRealm, "Sub", "Nested").executeIt()
        AddFlowExecutionAction(testRealm, "Nested", "auth-cookie", "cookie").executeIt()

        action.undoIt()

        assertThat(client.flowExecutions(testRealm, topLevelFlow)).isEmpty()
        assertThatThrownBy { client.flowExecutions(testRealm, "Sub") }.isInstanceOf(FeignException::class.java)
        assertThatThrownBy { client.flowExecutions(testRealm, "Nested") }.isInstanceOf(FeignException::class.java)
    }
}
