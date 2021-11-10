package de.klg71.keycloakmigration.changeControl.actions.flow

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class DeleteFlowIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteFlow() {
        val alias = "FloRida"
        AddFlowAction(
            testRealm, alias, "Right round", executions = listOf(
                AuthenticationExecutionImport(Flow.Requirement.REQUIRED, "idp-auto-link", 0, 0, mapOf())
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
}
