package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.MASKED_VALUE
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class UpdateFlowIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testUpdateFlow() {
        val alias = "FloRida"
        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(),
                    Flow.Requirement.REQUIRED,
                    "idp-auto-link",
                    0,
                    0,
                    0,
                    // No undeclared config keys: Keycloak masks them, and updateFlow refuses to replace those
                    mapOf()
                )
            )
        ).executeIt()

        val flow = client.flows(testRealm).first { it.alias == alias }
        val newAlias = "FloRider"
        val description = "Left round"
        UpdateFlowAction(
            testRealm, flow.alias, newAlias, description, null, null, listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(),Flow.Requirement.REQUIRED, "idp-confirm-link", 0, 0, 0, mapOf("foo1" to "bar1")
                )
            )
        ).executeIt()
        val updatedFlow = client.flows(testRealm).first { it.alias == newAlias }
        val executions = client.flowExecutions(testRealm, newAlias)
        assertThat(updatedFlow.description).isEqualTo(description)
        assertThat(executions).hasSize(1)
        assertThat(executions.first().providerId).isEqualTo("idp-confirm-link")
        assertThat(executions.first().requirement).isEqualTo(Flow.Requirement.REQUIRED)
        val authenticatorConfiguration =
            client.getAuthenticatorConfiguration(testRealm, executions.first().authenticationConfig!!)
        assertThat(authenticatorConfiguration.config.keys).containsExactlyInAnyOrder("foo1")
        assertThat(authenticatorConfiguration.config.values).allMatch { it == MASKED_VALUE }
    }

    @Test
    fun testUpdateFlow_Rollback() {
        val alias = "FloRida"
        val originalDescription = "Right round"
        AddFlowAction(
            testRealm, alias, originalDescription, executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(),
                    Flow.Requirement.REQUIRED,
                    "idp-auto-link",
                    0,
                    0,
                    0,
                    // No undeclared config keys: Keycloak masks them, and updateFlow refuses to replace those
                    mapOf()
                )
            )
        ).executeIt()

        val flow = client.flows(testRealm).first { it.alias == alias }
        val newAlias = "FloRider"
        val description = "Left round"
        val action = UpdateFlowAction(
            testRealm, flow.alias, newAlias, description, null, null, listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(),Flow.Requirement.REQUIRED, "idp-confirm-link", 0, 0, 0, mapOf("foo1" to "bar1")
                )
            )
        )
        action.executeIt()
        val updatedFlow = client.flows(testRealm).first { it.alias == newAlias }
        assertThat(updatedFlow.description).isEqualTo(description)
        action.undoIt()
        val oldFlow = client.flows(testRealm).first { it.alias == alias }
        assertThat(oldFlow.description).isEqualTo(originalDescription)
    }

    private data class Step(val name: String, val level: Int, val requirement: Flow.Requirement, val priority: Int)

    private fun steps(alias: String) = client.flowExecutions(testRealm, alias).map {
        Step(it.providerId ?: it.displayName, it.level, it.requirement, it.priority)
    }

    private fun redirectorConfig(alias: String) = client.flowExecutions(testRealm, alias)
        .single { it.providerId == "identity-provider-redirector" }
        .let { client.getAuthenticatorConfiguration(testRealm, it.authenticationConfig!!) }

    // A flow with a top-level execution, a sub flow holding a nested sub flow, and a configured execution
    private fun addNestedFlow(alias: String) {
        AddFlowAction(
            testRealm, alias, "Nested", executions = listOf(
                AuthenticationExecutionImport(UUID.randomUUID(), Flow.Requirement.ALTERNATIVE, "auth-cookie", 0, 0, 10)
            )
        ).executeIt()
        AddSubFlowAction(
            testRealm, alias, "$alias Forms", description = "the forms", requirement = Flow.Requirement.ALTERNATIVE,
            priority = 20, executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(), Flow.Requirement.ALTERNATIVE, "identity-provider-redirector", 1, 0, 10,
                    mapOf("defaultProvider" to "some-idp")
                ),
                AuthenticationExecutionImport(
                    UUID.randomUUID(), Flow.Requirement.REQUIRED, "auth-username-password-form", 1, 1, 20
                )
            )
        ).executeIt()
        AddSubFlowAction(
            testRealm, "$alias Forms", "$alias MFA", requirement = Flow.Requirement.CONDITIONAL, priority = 30,
            executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(), Flow.Requirement.REQUIRED, "conditional-user-configured", 2, 0, 10
                ),
                AuthenticationExecutionImport(
                    UUID.randomUUID(), Flow.Requirement.ALTERNATIVE, "auth-otp-form", 2, 1, 20
                )
            )
        ).executeIt()
    }

    @Test
    fun testUpdateFlowWithoutExecutionsKeepsSubFlows() {
        val alias = "KeepNested"
        addNestedFlow(alias)
        val before = steps(alias)

        UpdateFlowAction(testRealm, alias, description = "new description").executeIt()

        assertThat(client.flows(testRealm).first { it.alias == alias }.description).isEqualTo("new description")
        assertThat(steps(alias)).isEqualTo(before)
    }

    @Test
    fun testUpdateFlowRollbackRestoresSubFlows() {
        val alias = "RestoreNested"
        val newAlias = "RestoreNestedRenamed"
        addNestedFlow(alias)
        val before = steps(alias)

        val action = UpdateFlowAction(
            testRealm, alias, newAlias, "Replaced", executions = listOf(
                AuthenticationExecutionImport(UUID.randomUUID(), Flow.Requirement.REQUIRED, "idp-auto-link", 0, 0, 10)
            )
        )
        action.executeIt()
        assertThat(steps(newAlias).map { it.name }).containsExactly("idp-auto-link")

        action.undoIt()

        assertThat(client.flows(testRealm).single { it.alias == alias }.description).isEqualTo("Nested")
        assertThat(client.flows(testRealm).none { it.alias == newAlias }).isTrue
        assertThat(steps(alias)).isEqualTo(before)
        assertThat(client.flowExecutions(testRealm, "$alias MFA").map { it.providerId })
            .containsExactly("conditional-user-configured", "auth-otp-form")
        val forms = client.flowExecutions(testRealm, alias).single { it.displayName == "$alias Forms" }
        assertThat(forms.description).isEqualTo("the forms")
        val config = redirectorConfig(alias)
        assertThat(config.alias).isEqualTo("identity-provider-redirector_config")
        assertThat(config.config).isEqualTo(mapOf("defaultProvider" to "some-idp"))
    }

    @Test
    fun testUpdateFlowFailureRollsBack() {
        val alias = "FailNested"
        addNestedFlow(alias)
        val before = steps(alias)

        assertThatThrownBy {
            UpdateFlowAction(
                testRealm, alias, executions = listOf(
                    AuthenticationExecutionImport(
                        UUID.randomUUID(), Flow.Requirement.REQUIRED, "auth-cookie", 0, 0, 10
                    ),
                    AuthenticationExecutionImport(
                        UUID.randomUUID(), Flow.Requirement.REQUIRED, "no-such-provider", 0, 1, 20
                    )
                )
            ).executeIt()
        }

        assertThat(steps(alias)).isEqualTo(before)
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
    fun testReplacingExecutionsWithMaskedConfigIsRefused() {
        val alias = "MaskedFlow"
        addFlowWithMaskedConfig(alias)
        val ids = client.flowExecutions(testRealm, alias).map { it.id }

        assertThatThrownBy {
            UpdateFlowAction(
                testRealm, alias, executions = listOf(
                    AuthenticationExecutionImport(
                        UUID.randomUUID(), Flow.Requirement.REQUIRED, "idp-auto-link", 0, 0, 10
                    )
                )
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessageContaining("can't be rolled back")

        assertThat(client.flowExecutions(testRealm, alias).map { it.id }).isEqualTo(ids)
    }

    @Test
    fun testUpdatingOnlyTheFlowWithMaskedConfigIsAllowed() {
        val alias = "MaskedFlow"
        addFlowWithMaskedConfig(alias)

        UpdateFlowAction(testRealm, alias, description = "changed").executeIt()

        assertThat(client.flows(testRealm).single { it.alias == alias }.description).isEqualTo("changed")
    }

    @Test
    fun testFailedFlowUpdateLeavesExecutionsAlone() {
        val alias = "RenameFails"
        addNestedFlow(alias)
        AddFlowAction(testRealm, "TakenAlias").executeIt()
        val ids = client.flowExecutions(testRealm, alias).map { it.id }

        assertThatThrownBy {
            UpdateFlowAction(
                testRealm, alias, newAlias = "TakenAlias", executions = listOf(
                    AuthenticationExecutionImport(
                        UUID.randomUUID(), Flow.Requirement.REQUIRED, "idp-auto-link", 0, 0, 10
                    )
                )
            ).executeIt()
        }

        assertThat(client.flowExecutions(testRealm, alias).map { it.id }).isEqualTo(ids)
    }
}
