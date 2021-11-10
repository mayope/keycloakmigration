package de.klg71.keycloakmigration.changeControl.actions.user

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class DeleteUserAttributeIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteUserAttribute() {
        AddUserAction(testRealm, "test").executeIt()
        AddUserAttributeAction(testRealm, "test", "testAttribute", listOf("testValue1", "testValue2")).executeIt()
        DeleteUserAttributeAction(testRealm, "test", "testAttribute").executeIt()

        client.userByName("test", testRealm).let {
            assertThat(it.attributes).isNullOrEmpty()
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
