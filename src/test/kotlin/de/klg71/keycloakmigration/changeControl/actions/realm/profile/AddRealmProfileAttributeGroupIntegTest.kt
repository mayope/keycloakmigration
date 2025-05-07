package de.klg71.keycloakmigration.changeControl.actions.realm.profile

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.RealmAttributeGroup
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class AddRealmProfileAttributeGroupIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddRealmProfileAttributeGroup() {
        val name = "test-attribute-grp"
        val displayName = "Test Attribute Group"
        val description = "Test description"
        val annotations = mapOf("annotation1" to "value1")

        AddRealmProfileAttributeGroupAction(
            testRealm,
            name,
            displayName,
            description,
            annotations
        ).executeIt()

        val groups = client.realmUserProfile(testRealm).groups

        val expected = RealmAttributeGroup(name, displayName, description, annotations)

        assertThat(groups).hasSize(2)
        assertThat(groups.last()).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun testAddRealmProfileAttributeGroup_WithMandatoryFieldsOnly() {
        val name = "test-attribute-grp"

        AddRealmProfileAttributeGroupAction(testRealm, name, null, null).executeIt()

        val groups = client.realmUserProfile(testRealm).groups

        val expected = RealmAttributeGroup(name, null, null)

        assertThat(groups).hasSize(2)
        assertThat(groups.last()).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun testAddRealmProfileAttributeGroup_Rollback() {
        val name = "test-attribute-grp"
        val displayName = "Test Attribute Group"
        val description = "Test description"
        val annotations = mapOf("annotation1" to "value1")

        val action = AddRealmProfileAttributeGroupAction(
            testRealm,
            name,
            displayName,
            description,
            annotations
        )

        action.executeIt()
        var groups = client.realmUserProfile(testRealm).groups

        assertThat(groups).hasSize(2)

        action.undoIt()
        groups = client.realmUserProfile(testRealm).groups

        val deletedGroup = RealmAttributeGroup(name, displayName, description, annotations)

        assertThat(groups).hasSize(1)
        assertThat(groups.first()).usingRecursiveComparison().isNotEqualTo(deletedGroup)
    }
}