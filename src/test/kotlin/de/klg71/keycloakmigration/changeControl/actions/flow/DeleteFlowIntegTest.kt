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

class DeleteFlowIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteFlow() {
        val alias = "FloRida"
        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(),
                    Flow.Requirement.REQUIRED,
                    "idp-auto-link",
                    0, 0, 0,mapOf())
            )
        ).executeIt()

        var flows = client.flows(testRealm)
        assertThat(flows.any { it.alias == alias }).isTrue

        DeleteFlowAction(testRealm, alias).executeIt()

        flows = client.flows(testRealm)
        assertThat(flows.any { it.alias == alias }).isFalse
    }

    @Test
    fun testDeleteFlow_notExisting() {
        val alias = "FloRida"

        DeleteFlowAction(testRealm, alias).executeIt()

        val flows = client.flows(testRealm)
        assertThat(flows.any { it.alias == alias }).isFalse
    }

    private data class Step(val name: String, val level: Int, val requirement: Flow.Requirement, val priority: Int)

    private fun steps(alias: String) = client.flowExecutions(testRealm, alias).map {
        Step(it.providerId ?: it.displayName, it.level, it.requirement, it.priority)
    }

    @Test
    fun testDeleteFlowRollbackRestoresSubFlows() {
        val alias = "DeleteNested"
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
        val before = steps(alias)

        val action = DeleteFlowAction(testRealm, alias)
        action.executeIt()
        assertThat(client.flows(testRealm).none { it.alias == alias }).isTrue

        action.undoIt()

        assertThat(client.flows(testRealm).single { it.alias == alias }.description).isEqualTo("Nested")
        assertThat(steps(alias)).isEqualTo(before)
        val redirector = client.flowExecutions(testRealm, alias)
            .single { it.providerId == "identity-provider-redirector" }
        val config = client.getAuthenticatorConfiguration(testRealm, redirector.authenticationConfig!!)
        assertThat(config.config).isEqualTo(mapOf("defaultProvider" to "some-idp"))
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
    fun testDeleteFlowWithMaskedConfigIsRefused() {
        val alias = "MaskedFlow"
        addFlowWithMaskedConfig(alias)

        assertThatThrownBy { DeleteFlowAction(testRealm, alias).executeIt() }
            .isInstanceOf(MigrationException::class.java).hasMessageContaining("can't be rolled back")

        assertThat(client.flows(testRealm).any { it.alias == alias }).isTrue
    }
}
