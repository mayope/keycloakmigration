package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.GroupListItem
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.groupByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.standalone.inject
import java.util.*

class UpdateGroupIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    private val groupName = "integrationTest"

    @Test
    fun testUpdateAttributes() {
        AddGroupAction("master", groupName).executeIt()
        val attributes = mapOf("test" to listOf("test"))
        UpdateGroupAction("master", groupName,attributes,null,null).executeIt()

        assertThat(client.groupByName(groupName,"master").attributes).isEqualTo(attributes)
    }

    @After
    fun cleanup() {
        DeleteGroupAction("master", "integrationTest").executeIt()
    }
}