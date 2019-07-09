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
        AddUserAction(testRealm, "test").executeIt()
        AddUserAttributeAction(testRealm, "test", "testAttribute", listOf("testValue1", "testValue2")).executeIt()
        DeleteUserAttributeAction(testRealm, "test", "testAttribute").executeIt()

        client.userByName("test", testRealm).let {
            assertThat(it.attributes).doesNotContainEntry("testAttribute", listOf("testValue1", "testValue2"))
        }
    }

    @Test
    fun testDeleteUserAttribute_notExisting() {
        AddUserAction(testRealm, "test").executeIt()
        assertThatThrownBy {
            DeleteUserAttributeAction(testRealm, "test", "testAttribute").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Attribute testAttribute is not present on user test!")
    }
    @Test
    fun testDeleteUserAttribute_notExisting_failOnMissingFalse() {
        AddUserAction(testRealm, "test").executeIt()
        DeleteUserAttributeAction(testRealm, "test", "testAttribute", false).executeIt()
    }
}