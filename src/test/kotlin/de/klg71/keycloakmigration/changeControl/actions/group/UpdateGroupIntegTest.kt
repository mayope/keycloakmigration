package de.klg71.keycloakmigration.changeControl.actions.group

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.groupByName
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.standalone.inject

class UpdateGroupIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    private val groupName = "integrationTest"
    val realm = testRealm

    @Before
    fun setupTest() {
        AddGroupAction(realm, groupName).executeIt()
        AddGroupAction(realm, "Möbel test").executeIt()
        AddGroupAction(realm, "Otto").executeIt()
        AddGroupAction(realm, "Möbel", "Otto").executeIt()
    }

    @Test
    fun testUpdateAttributes() {
        val attributes = mapOf("test" to listOf("test"))
        UpdateGroupAction(realm, groupName, attributes, null, null).executeIt()

        assertThat(client.groupByName(groupName, realm).attributes).isEqualTo(attributes)
    }

    @Test
    fun testUpdateSpecialCharacters() {
        UpdateGroupAction(realm, "Möbel", null, null, null).executeIt()
    }
}