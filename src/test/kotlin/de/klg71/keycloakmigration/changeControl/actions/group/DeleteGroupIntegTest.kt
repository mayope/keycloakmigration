package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.existsGroup
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject

class DeleteGroupIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteGroup() {
        AddGroupAction("master", "integrationTest").executeIt()
        DeleteGroupAction("master", "integrationTest").executeIt()

        assertThat(client.existsGroup("integrationTest", "master")).isFalse()
    }

    @Test
    fun testDeleteGroupNotExisting() {
        assertThatThrownBy {
            DeleteGroupAction("master", "integrationTest").executeIt()
        }.isInstanceOf(MigrationException::class.java)
                .hasMessage("Group with name: integrationTest does not exist in realm: master!")
    }
}