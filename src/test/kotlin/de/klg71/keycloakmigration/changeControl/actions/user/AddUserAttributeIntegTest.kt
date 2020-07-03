package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.userByName
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.inject

class AddUserAttributeIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddAttribute() {
        AddUserAction(testRealm, "test").executeIt()
        AddUserAttributeAction(testRealm, "test", "testAttribute", listOf("testValue1", "testValue2")).executeIt()

        client.userByName("test", testRealm).let {
            assertThat(it.attributes).containsEntry("testAttribute", listOf("testValue1", "testValue2"))
        }
    }

    @Test
    fun testAddAttribute_alreadyExistsOverrideFalse() {
        AddUserAction(testRealm, "test").executeIt()
        AddUserAttributeAction(testRealm, "test", "testAttribute", listOf("testValue1", "testValue2")).executeIt()
        Assertions.assertThatThrownBy {
            AddUserAttributeAction(testRealm, "test", "testAttribute", listOf("testValue1", "testValue2")).executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Attribute testAttribute is already present on user test!")
    }

    @Test
    fun testAddAttribute_alreadyExistsOverrideTrue() {
        AddUserAction(testRealm, "test").executeIt()
        AddUserAttributeAction(testRealm, "test", "testAttribute", listOf("testValue1", "testValue2")).executeIt()
        AddUserAttributeAction(testRealm, "test", "testAttribute", listOf("testValue2", "testValue3"), true).executeIt()

        client.userByName("test", testRealm).let {
            assertThat(it.attributes).containsEntry("testAttribute", listOf("testValue2", "testValue3"))
        }
    }
}
