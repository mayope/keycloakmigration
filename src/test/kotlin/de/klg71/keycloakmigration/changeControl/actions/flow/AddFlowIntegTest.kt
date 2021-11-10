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

class AddFlowIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddFlow() {
        val alias = "FloRida"
        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(
                        Flow.Requirement.REQUIRED,
                        "idp-auto-link",
                        0, 0,
                        mapOf("foo" to "bar", "foo1" to "bar1")
                )
            )
        ).executeIt()

        val flows = client.flows(testRealm)
        assertThat(flows.any { it.alias == alias }).isTrue
        val executions = client.flowExecutions(testRealm, alias)
        assertThat(executions).hasSize(1)
        assertThat(executions.first().providerId).isEqualTo("idp-auto-link")
        assertThat(executions.first().requirement).isEqualTo(Flow.Requirement.REQUIRED)
        assertThat(executions.first().index).isEqualTo(0)
        assertThat(executions.first().level).isEqualTo(0)
        assertThat(executions.first().authenticationConfig != null)
        val authenticatorConfiguration = client.getAuthenticatorConfiguration(testRealm, executions.first().authenticationConfig!!)
        assertThat(authenticatorConfiguration.config).containsExactly(
                AbstractMap.SimpleEntry("foo", "bar"),
                AbstractMap.SimpleEntry("foo1", "bar1"),
        )
    }

    @Test
    fun testAddMulti() {
        val alias = "FloRida"
        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(
                        Flow.Requirement.REQUIRED,
                        "idp-auto-link",
                        0, 0,
                        mapOf("foo" to "bar", "foo1" to "bar1")
                ),
                AuthenticationExecutionImport(
                        Flow.Requirement.ALTERNATIVE,
                        "idp-confirm-link",
                        0, 1,
                        mapOf("foo2" to "bar2", "foo3" to "bar3")
                ),
            )
        ).executeIt()

        val executions = client.flowExecutions(testRealm, alias)
        assertThat(executions).hasSize(2)
        assertThat(executions.first().providerId).isEqualTo("idp-auto-link")
        assertThat(executions.first().requirement).isEqualTo(Flow.Requirement.REQUIRED)
        val authenticatorConfiguration1 = client.getAuthenticatorConfiguration(testRealm, executions.first().authenticationConfig!!)
        assertThat(authenticatorConfiguration1.config).containsExactly(
                AbstractMap.SimpleEntry("foo", "bar"),
                AbstractMap.SimpleEntry("foo1", "bar1"),
        )
        assertThat(executions[1].providerId).isEqualTo("idp-confirm-link")
        assertThat(executions[1].requirement).isEqualTo(Flow.Requirement.ALTERNATIVE)
        val authenticatorConfiguration2 = client.getAuthenticatorConfiguration(testRealm, executions[1].authenticationConfig!!)
        assertThat(authenticatorConfiguration2.config).containsExactly(
                AbstractMap.SimpleEntry("foo2", "bar2"),
                AbstractMap.SimpleEntry("foo3", "bar3"),
        )
    }

    @Test
    fun testAddFlow_Existing() {
        val alias = "FloRida"
        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(Flow.Requirement.REQUIRED, "idp-auto-link", 0, 0, mapOf())
            )
        ).executeIt()
        assertThatThrownBy {
            AddFlowAction(
                testRealm, alias, "Right round", executions = listOf(
                    AuthenticationExecutionImport(Flow.Requirement.REQUIRED, "idp-auto-link", 0, 0, mapOf())
                )
            ).executeIt()
        }.isInstanceOf(KeycloakApiException::class.java).hasMessage("Import Flow failed, Flow: $alias already exists")
    }
}
