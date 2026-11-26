package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

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

    private data class Step(val name: String, val level: Int, val requirement: Flow.Requirement, val priority: Int)

    private fun steps(alias: String) = client.flowExecutions(testRealm, alias).map {
        Step(it.providerId ?: it.displayName, it.level, it.requirement, it.priority)
    }

    private fun addFlowWithSubFlow(alias: String, subFlow: String) {
        AddFlowAction(
            testRealm, alias, executions = listOf(
                AuthenticationExecutionImport(UUID.randomUUID(), Flow.Requirement.ALTERNATIVE, "auth-cookie", 0, 0, 10)
            )
        ).executeIt()
        AddSubFlowAction(
            testRealm, alias, subFlow, requirement = Flow.Requirement.ALTERNATIVE, priority = 20,
            executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(), Flow.Requirement.REQUIRED, "conditional-user-configured", 1, 0, 10
                ),
                AuthenticationExecutionImport(
                    UUID.randomUUID(), Flow.Requirement.ALTERNATIVE, "auth-otp-form", 1, 1, 20
                ),
                AuthenticationExecutionImport(
                    UUID.randomUUID(), Flow.Requirement.ALTERNATIVE, "webauthn-authenticator", 1, 2, 30
                )
            )
        ).executeIt()
    }

    @Test
    fun testUndoDeleteNestedFlowExecution() {
        val alias = "NestedFlow"
        addFlowWithSubFlow(alias, "NestedSub")
        val before = steps(alias)

        val deleteAction = DeleteFlowExecutionAction(testRealm, alias, "auth-otp-form")
        deleteAction.executeIt()

        assertThat(steps(alias)).isEqualTo(before.filterNot { it.name == "auth-otp-form" })
        assertThat(client.flowExecutions(testRealm, "NestedSub").map { it.providerId })
            .containsExactly("conditional-user-configured", "webauthn-authenticator")

        deleteAction.undoIt()

        assertThat(steps(alias)).isEqualTo(before)
        assertThat(client.flowExecutions(testRealm, "NestedSub").map { it.providerId })
            .containsExactly("conditional-user-configured", "auth-otp-form", "webauthn-authenticator")
    }

    @Test
    fun testUndoDeleteFlowExecutionRestoresConfig() {
        val alias = "ConfigFlow"
        val provider = "identity-provider-redirector"
        AddFlowAction(
            testRealm, alias, executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(), Flow.Requirement.ALTERNATIVE, provider, 0, 0, 10,
                    mapOf("defaultProvider" to "some-idp")
                )
            )
        ).executeIt()

        val deleteAction = DeleteFlowExecutionAction(testRealm, alias, provider)
        deleteAction.executeIt()
        deleteAction.undoIt()

        val restored = client.flowExecutions(testRealm, alias).single()
        assertThat(restored.providerId).isEqualTo(provider)
        assertThat(restored.requirement).isEqualTo(Flow.Requirement.ALTERNATIVE)
        val config = client.getAuthenticatorConfiguration(testRealm, restored.authenticationConfig!!)
        assertThat(config.alias).isEqualTo("${provider}_config")
        assertThat(config.config).isEqualTo(mapOf("defaultProvider" to "some-idp"))
    }

    @Test
    fun testDeleteSubFlowIsRefused() {
        val alias = "FormParent"
        AddFlowAction(testRealm, alias).executeIt()
        AddSubFlowAction(
            testRealm, alias, "FormSub", type = "form-flow", provider = "registration-page-form",
            requirement = Flow.Requirement.REQUIRED
        ).executeIt()
        val before = steps(alias)

        assertThatThrownBy {
            DeleteFlowExecutionAction(testRealm, alias, "registration-page-form").executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("is a subflow")

        assertThat(steps(alias)).isEqualTo(before)
    }

    @Test
    fun testUndoWhenFlowDoesNotExist() {
        val deleteAction = DeleteFlowExecutionAction(testRealm, "MissingFlow", "auth-cookie")
        deleteAction.executeIt()
        deleteAction.undoIt()
    }

    // Keycloak masks config keys the provider doesn't declare, so this config can't be read back
    private fun addFlowWithMaskedConfig(alias: String) = AddFlowAction(
        testRealm, alias, executions = listOf(
            AuthenticationExecutionImport(
                UUID.randomUUID(), Flow.Requirement.REQUIRED, "idp-confirm-link", 0, 0, 10, mapOf("custom" to "value")
            )
        )
    ).executeIt()

    @Test
    fun testDeleteFlowExecutionWithMaskedConfigIsRefused() {
        val alias = "MaskedFlow"
        addFlowWithMaskedConfig(alias)

        assertThatThrownBy {
            DeleteFlowExecutionAction(testRealm, alias, "idp-confirm-link").executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessageContaining("can't be rolled back")

        assertThat(client.flowExecutions(testRealm, alias).map { it.providerId }).containsExactly("idp-confirm-link")
    }
}
