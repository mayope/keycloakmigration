package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject

class DeleteUserAttributeIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteUserAttribute() {
        AddUserAction("master", "test").executeIt()
        AddUserAttributeAction("master", "test", "testAttribute", listOf("testValue1", "testValue2")).executeIt()
        DeleteUserAttributeAction("master", "test", "testAttribute").executeIt()

        client.userByName("test", "master").let {
            assertThat(it.attributes).doesNotContainEntry("testAttribute", listOf("testValue1", "testValue2"))
        }
    }

    @Test
    fun testDeleteUserAttribute_notExisting() {
        AddUserAction("master", "test").executeIt()
        assertThatThrownBy {
            DeleteUserAttributeAction("master", "test", "testAttribute").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Attribute testAttribute is not present on user test!")
    }
    @Test
    fun testDeleteUserAttribute_notExisting_failOnMissingFalse() {
        AddUserAction("master", "test").executeIt()
        DeleteUserAttributeAction("master", "test", "testAttribute", false).executeIt()
    }

    @After
    fun cleanup() {
        DeleteUserAction("master", "test").executeIt()
    }
}