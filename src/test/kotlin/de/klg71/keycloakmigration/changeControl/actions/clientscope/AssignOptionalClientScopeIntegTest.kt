package de.klg71.keycloakmigration.changeControl.actions.clientscope

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AssignOptionalClientScopeIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val scopeName = "integrationTest"
    private val clientId = "integrationTest"

    @Test
    fun testAssignOptionalClientScope() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AssignOptionalClientScopeAction(testRealm, scopeName, clientId).executeIt()

        val scopes = client.optionalClientScopes(testRealm, client.clientUUID(clientId, testRealm))
        assertThat(scopes.any { it.name == scopeName }).isTrue()
    }

    @Test
    fun testAssignOptionalClientScope_clientScopeMissing() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        assertThatThrownBy {
            AssignOptionalClientScopeAction(testRealm, scopeName, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("ClientScope with name: $scopeName does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testAssignOptionalClientScope_clientMissing() {
        AddClientScopeAction(testRealm, scopeName).executeIt()

        assertThatThrownBy {
            AssignOptionalClientScopeAction(testRealm, scopeName, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Client with id: $clientId does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testAssignOptionalClientScope_undo() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AssignOptionalClientScopeAction(testRealm, scopeName, clientId).run {
            executeIt()
            undoIt()
        }

        val scopes = client.optionalClientScopes(testRealm, client.clientUUID(clientId, testRealm))
        assertThat(scopes.any { it.name == scopeName }).isFalse()
    }
}
