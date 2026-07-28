package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.realm.UpdateRealmAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.Organization
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationDomain
import de.klg71.keycloakmigration.keycloakapi.organizationByAlias
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject
import java.util.UUID
import kotlin.getValue

class UpdateOrganizationIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testUpdateOrganization() {
        val alias = "alias"

        UpdateRealmAction(testRealm, organizationsEnabled = true).executeIt()
        val addOrganizationAction = AddOrganizationAction(
            testRealm,
            "name",
            alias,
            "http://redirectUrl.com",
            setOf(OrganizationDomain("test.com")),
            mapOf("custom-attribute" to listOf("values"))
        )
        addOrganizationAction.executeIt()

        UpdateOrganizationAction(
            testRealm,
            alias,
            name = "updated-name",
            redirectUrl = "http://updatedRedirectUrl.com",
            domains = setOf(OrganizationDomain("updated-test.com")),
            attributes = mapOf("custom-attribute" to listOf("updated-values"))
        ).executeIt()

        val expected = Organization(
            id = UUID.randomUUID(),
            name = "updated-name",
            alias = "alias",
            redirectUrl = "http://updatedRedirectUrl.com",
            domains = setOf(OrganizationDomain("updated-test.com")),
            attributes = mapOf("custom-attribute" to listOf("updated-values"))
        )

        val actual = client.organizationByAlias(alias, testRealm)

        assertThat(expected)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(actual)
    }

    @Test
    fun testUndoUpdateOrganization() {
        val alias = "alias"
        val originalName = "name"
        val originalRedirectUrl = "http://redirectUrl.com"
        val originalDomains = setOf(OrganizationDomain("test.com"))
        val originalAttributes = mapOf("custom-attribute" to listOf("values"))

        UpdateRealmAction(testRealm, organizationsEnabled = true).executeIt()
        val addOrganizationAction = AddOrganizationAction(
            testRealm,
            originalName,
            alias,
            originalRedirectUrl,
            originalDomains,
            originalAttributes
        )
        addOrganizationAction.executeIt()

        val updateOrganizationAction = UpdateOrganizationAction(
            testRealm,
            alias,
            name = "updated-name",
            redirectUrl = "http://updatedRedirectUrl.com",
            domains = setOf(OrganizationDomain("updated-test.com")),
            attributes = mapOf("custom-attribute" to listOf("updated-values"))
        )
        updateOrganizationAction.executeIt()

        updateOrganizationAction.undoIt()

        val expected = Organization(
            id = UUID.randomUUID(),
            name = originalName,
            alias = alias,
            redirectUrl = originalRedirectUrl,
            domains = originalDomains,
            attributes = originalAttributes
        )

        val actual = client.organizationByAlias(alias, testRealm)

        assertThat(expected)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(actual)
    }}
