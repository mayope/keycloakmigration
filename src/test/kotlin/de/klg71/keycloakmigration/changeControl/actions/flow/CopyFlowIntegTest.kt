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
import java.util.AbstractMap

class CopyFlowIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testCopyFlow() {
        val alias = "CopyTestFLow"
        AddFlowAction(
            testRealm, "TestFLow", "Right round", executions = listOf(
                AuthenticationExecutionImport(
                    Flow.Requirement.REQUIRED,
                    "idp-auto-link",
                    0, 0, 0,
                    mapOf("foo" to "bar", "foo1" to "bar1")
                )
            )
        ).executeIt()
        CopyFlowAction(testRealm, flowAlias = "TestFLow", newName = alias).executeIt()

        val flows = client.flows(testRealm)
        assertThat(flows.any { it.alias == alias }).isTrue
        val executions = client.flowExecutions(testRealm, alias)
        assertThat(executions).hasSize(1)
        assertThat(executions.first().providerId).isEqualTo("idp-auto-link")
        assertThat(executions.first().requirement).isEqualTo(Flow.Requirement.REQUIRED)
        assertThat(executions.first().index).isEqualTo(0)
        assertThat(executions.first().level).isEqualTo(0)
        assertThat(executions.first().authenticationConfig != null)
        val authenticatorConfiguration =
            client.getAuthenticatorConfiguration(testRealm, executions.first().authenticationConfig!!)
        assertThat(authenticatorConfiguration.config).containsExactly(
            AbstractMap.SimpleEntry("foo", "bar"),
            AbstractMap.SimpleEntry("foo1", "bar1"),
        )
    }

    @Test
    fun testCopyFlow_NotExisting() {
        assertThatThrownBy {
            CopyFlowAction(testRealm, flowAlias = "TestFLow", newName = "CopyTestFLow").executeIt()
        }.isInstanceOf(KeycloakApiException::class.java).hasMessage("Copy Flow failed, Flow: TestFLow does not exist")
    }

    @Test
    fun testCopyFlow_Existing() {
        val alias = "TestFLow"
        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(Flow.Requirement.REQUIRED, "idp-auto-link", 0, 0, 0, mapOf())
            )
        ).executeIt()
        assertThatThrownBy {
            CopyFlowAction(testRealm, flowAlias = alias, newName = alias).executeIt()
        }.isInstanceOf(KeycloakApiException::class.java).hasMessage("Copy Flow failed, Flow: TestFLow already exists")
    }
}
