package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.identityprovider.AddIdentityProviderAction
import de.klg71.keycloakmigration.changeControl.actions.realm.UpdateRealmAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationDomain
import de.klg71.keycloakmigration.keycloakapi.organizationByName
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.core.component.inject

class LinkIdentityProviderToOrganizationActionTest : AbstractIntegrationTest() {

    private val client by inject<KeycloakClient>()

    @Test
    fun testLinkIdentityProviderToOrganization() {
        val orgName = "test-org"
        val idpAlias = "test-idp"

        // 1. Enable organizations feature on the realm
        UpdateRealmAction(testRealm, organizationsEnabled = true).executeIt()

        // 2. Create the target organization
        AddOrganizationAction(
            realm = testRealm,
            name = orgName,
            domains = setOf(OrganizationDomain("test-org.com"))
        ).executeIt()

        // 3. Create an identity provider to link
        AddIdentityProviderAction(
            realm = testRealm,
            alias = idpAlias,
            providerId = "oidc",
            config = mapOf(
                "authorizationUrl" to "https://auth.test.com",
                "tokenUrl" to "https://auth.test.com/token",
                "clientId" to "test-client",
                "clientSecret" to "test-secret"
            )
        ).executeIt()

        // 4. Execute action: link IdP to Organization
        LinkIdentityProviderToOrganizationAction(
            realm = testRealm,
            organizationAlias = orgName,
            identityProviderAlias = idpAlias
        ).executeIt()

        // 5. Assert: Verify the organization has the linked IdP
        val org = client.organizationByName(orgName, testRealm)
        val linkedIdps = client.organizationIdentityProviders(testRealm, org.id.toString())

        assertThat(linkedIdps.map { it.alias }).contains(idpAlias)
    }

    @Test
    fun testUndoLinkIdentityProviderToOrganization() {
        val orgName = "test-org-undo"
        val idpAlias = "test-idp-undo"

        UpdateRealmAction(testRealm, organizationsEnabled = true).executeIt()

        AddOrganizationAction(
            realm = testRealm,
            name = orgName,
            domains = setOf(OrganizationDomain("test-org-undo.com"))
        ).executeIt()

        AddIdentityProviderAction(
            realm = testRealm,
            alias = idpAlias,
            providerId = "oidc",
            config = mapOf(
                "authorizationUrl" to "https://auth.test.com",
                "tokenUrl" to "https://auth.test.com/token",
                "clientId" to "test-client",
                "clientSecret" to "test-secret"
            )
        ).executeIt()

        val action = LinkIdentityProviderToOrganizationAction(
            realm = testRealm,
            organizationAlias = orgName,
            identityProviderAlias = idpAlias
        )

        // Link and then undo link
        action.executeIt()
        action.undoIt()

        // Assert: Verify the IdP is no longer linked to the organization
        val org = client.organizationByName(orgName, testRealm)
        val linkedIdps = client.organizationIdentityProviders(testRealm, org.id.toString())

        assertThat(linkedIdps.map { it.alias }).doesNotContain(idpAlias)
    }
}
