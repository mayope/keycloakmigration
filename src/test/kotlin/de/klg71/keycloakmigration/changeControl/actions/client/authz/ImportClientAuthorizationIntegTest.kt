package de.klg71.keycloakmigration.changeControl.actions.client.authz

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class ImportClientAuthorizationIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testImportClientAuthorization() {
        // given
        AddSimpleClientAction(
            testRealm,
            "integ-test-client-authorization",
            true,
            authorizationServicesEnabled = true,
            serviceAccountsEnabled = true,
            publicClient = false
        ).executeIt()

        // when
        ImportClientAuthorizationAction(
            testRealm,
            "integ-test-client-authorization",
            "integ-test-client-authorization.json"
        ).apply {
            path = "src/test/resources/changesets"
        }.executeIt()

        // then
        val testClient = client.clientById("integ-test-client-authorization", testRealm)
        val authorizationResources = client.clientAuthorizationResources(testClient.id, testRealm);
        val authorizationResourceNames = authorizationResources.map { it.name }

        assertThat(authorizationResourceNames).contains("integ-client-authorization-resource")
    }
}
