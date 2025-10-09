package de.klg71.keycloakmigration.changeControl.actions.realm.profile

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class UpdateRealmProfileOrderIntegTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testReorderAttributes_Success() {
        val orderedAttributes = listOf("lastName", "firstName", "email", "username")

        UpdateRealmProfileOrderAction(
            testRealm,
            orderedAttributes
        ).executeIt()

        val updatedAttributes = client.realmUserProfile(testRealm).attributes
        val updatedNames = updatedAttributes.map { it.name }

        assertThat(updatedNames).isEqualTo(orderedAttributes)
    }

    @Test
    fun testReorderAttributes_AttributeInOrderNotInProfile_Throws() {
        val orderedAttributes = listOf("lastName", "extraAttribute", "email", "username")

        assertThatThrownBy {
            UpdateRealmProfileOrderAction(testRealm, orderedAttributes).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("Attribute 'extraAttribute' does not exist in the realm profile!")
    }

    @Test
    fun testReorderAttributes_ProfileHasExtraAttr_Throws() {
        val orderedAttributes = listOf("lastName", "firstName", "email")

        assertThatThrownBy {
            UpdateRealmProfileOrderAction(testRealm, orderedAttributes).executeIt()
        }.isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("Attributes missing in new order: [username]")

    }
}
