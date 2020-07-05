package de.klg71.keycloakmigration.changeControl.actions.clientscope

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class AddClientScopeIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()
    private val scopeName = "integrationTest"

    @Test
    fun testAddScope() {
        AddClientScopeAction(testRealm, scopeName).executeIt()

        val scopes = client.clientScopes(testRealm)
        assertThat(scopes.any { it.name == scopeName }).isTrue()
    }

    @Test
    fun testAddScopeAlreadyExisting() {
        AddClientScopeAction(testRealm, scopeName).executeIt()
        assertThatThrownBy {
            AddClientScopeAction(testRealm, scopeName).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("ClientScope with name: integrationTest already exists in realm: ${testRealm}!")
    }

    @Test
    fun testUndoAddScope() {
        AddClientScopeAction(testRealm, scopeName).run {
            executeIt()
            undoIt()
        }
        val scopes = client.clientScopes(testRealm)
        assertThat(scopes.any { it.name == scopeName }).isFalse()
    }
}
