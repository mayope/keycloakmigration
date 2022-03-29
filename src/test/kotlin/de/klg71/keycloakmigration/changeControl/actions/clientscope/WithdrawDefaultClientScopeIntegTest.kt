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

class WithdrawDefaultClientScopeIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val scopeName = "integrationTest"
    private val clientId = "integrationTest"

    @Test
    fun testWithdrawDefaultClientScope() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AssignDefaultClientScopeAction(testRealm, scopeName, clientId).executeIt()
        WithdrawDefaultClientScopeAction(testRealm, scopeName, clientId).executeIt()

        val scopes = client.defaultClientScopes(testRealm, client.clientUUID(clientId, testRealm))
        assertThat(scopes.any { it.name == scopeName }).isFalse()
    }

    @Test
    fun testWithDrawDefaultClientScope_clientScopeMissing() {
        AddSimpleClientAction(testRealm, clientId).executeIt()

        assertThatThrownBy {
            WithdrawDefaultClientScopeAction(testRealm, scopeName, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("ClientScope with name: $scopeName does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testWithdrawDefaultClientScope_clientMissing() {
        AddClientScopeAction(testRealm, scopeName).executeIt()

        assertThatThrownBy {
            WithdrawDefaultClientScopeAction(testRealm, scopeName, clientId).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessage("Client with id: $clientId does not exist in realm: ${testRealm}!")
    }

    @Test
    fun testWithdrawDefaultClientScope_undo() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        AddSimpleClientAction(testRealm, clientId).executeIt()
        AssignDefaultClientScopeAction(testRealm, scopeName, clientId).executeIt()
        WithdrawDefaultClientScopeAction(testRealm, scopeName, clientId).run {
            executeIt()
            undoIt()
        }

        val scopes = client.defaultClientScopes(testRealm, client.clientUUID(clientId, testRealm))
        assertThat(scopes.any { it.name == scopeName }).isTrue()
    }
}
