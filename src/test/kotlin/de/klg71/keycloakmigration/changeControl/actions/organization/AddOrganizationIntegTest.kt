package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.actions.realm.UpdateRealmAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationDomain
import de.klg71.keycloakmigration.keycloakapi.organizationByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class AddOrganizationIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddOrganization() {
        val name = "test"

        UpdateRealmAction(testRealm, organizationsEnabled = true).executeIt()
        AddOrganizationAction(testRealm, name, domains = setOf(OrganizationDomain("test.com"))).executeIt()

        val org = client.organizationByName(name, testRealm)

        assertThat(org.name).isEqualTo(name)
        assertThat(org.alias).isEqualTo(name)
        assertThat(org.domains).isEqualTo(setOf(OrganizationDomain("test.com")))
    }

    @Test
    fun testAddOrganization_EmptyDomains() {
        UpdateRealmAction(testRealm, organizationsEnabled = true).executeIt()

        assertThatThrownBy {
            AddOrganizationAction(testRealm, "test", domains = setOf()).executeIt()
        }
            .isInstanceOf(MigrationException::class.java)
            .hasMessage("At least one domain needs to be provided!")
    }

    @Test
    fun testAddOrganization_Existing() {
        val name = "test"

        UpdateRealmAction(testRealm, organizationsEnabled = true).executeIt()
        AddOrganizationAction(testRealm, name, domains = setOf(OrganizationDomain("test.com"))).executeIt()

        assertThatThrownBy {
            AddOrganizationAction(testRealm, name, domains = setOf(OrganizationDomain("test.com"))).executeIt()
        }
            .isInstanceOf(MigrationException::class.java)
            .hasMessage("Organisation with name: test already exists!")
    }
}
