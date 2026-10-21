package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.identityprovider.AddIdentityProviderAction
import de.klg71.keycloakmigration.changeControl.actions.realm.UpdateRealmAction
import de.klg71.keycloakmigration.changeControl.actions.user.AddUserAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.allOrganizationMembers
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationDomain
import de.klg71.keycloakmigration.keycloakapi.organizationByAlias
import de.klg71.keycloakmigration.keycloakapi.userByName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject

class DeleteOrganizationIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    private val alias = "delete-alias"
    private val idpAlias = "delete-idp"

    private fun addOrganization() {
        UpdateRealmAction(testRealm, organizationsEnabled = true).executeIt()
        AddOrganizationAction(
            testRealm,
            "Delete Name",
            alias,
            "http://redirectUrl.com",
            setOf(OrganizationDomain("delete-test.com")),
            mapOf("custom-attribute" to listOf("values"))
        ).executeIt()
    }

    private fun addLinkedIdentityProvider() {
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
        LinkIdentityProviderToOrganizationAction(
            realm = testRealm,
            organizationAlias = alias,
            identityProviderAlias = idpAlias
        ).executeIt()
    }

    private val memberName = "delete-member"

    private fun addUnmanagedMember() {
        AddUserAction(testRealm, memberName).executeIt()
        val organization = client.organizationByAlias(alias, testRealm)
        val userId = client.userByName(memberName, testRealm).id.toString()
        assertThat(client.addOrganizationMember(testRealm, organization.id.toString(), userId).isSuccessful()).isTrue()
    }

    @Test
    fun testDeleteOrganization() {
        addOrganization()

        DeleteOrganizationAction(testRealm, alias).executeIt()

        assertThat(client.organizations(testRealm).map { it.alias }).doesNotContain(alias)
    }

    @Test
    fun testDeleteOrganizationKeepsLinkedIdentityProvider() {
        addOrganization()
        addLinkedIdentityProvider()

        DeleteOrganizationAction(testRealm, alias).executeIt()

        assertThat(client.identityProviders(testRealm).map { it.alias }).contains(idpAlias)
    }

    @Test
    fun testDeleteOrganizationKeepsUnmanagedMember() {
        addOrganization()
        addUnmanagedMember()

        DeleteOrganizationAction(testRealm, alias).executeIt()

        assertThat(client.userByName(memberName, testRealm).username).isEqualTo(memberName)
    }

    @Test
    fun testDeleteOrganizationNotExisting() {
        UpdateRealmAction(testRealm, organizationsEnabled = true).executeIt()

        assertThatThrownBy { DeleteOrganizationAction(testRealm, "missing-alias").executeIt() }
            .isInstanceOf(KeycloakApiException::class.java)
    }

    @Test
    fun testUndoDeleteOrganization() {
        addOrganization()
        addLinkedIdentityProvider()
        addUnmanagedMember()
        val original = client.organizationByAlias(alias, testRealm)

        val action = DeleteOrganizationAction(testRealm, alias)
        action.executeIt()
        action.undoIt()

        val restored = client.organizationByAlias(alias, testRealm)
        assertThat(restored)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(original)
        assertThat(client.organizationIdentityProviders(testRealm, restored.id.toString()).map { it.alias })
            .containsExactly(idpAlias)
        assertThat(client.allOrganizationMembers(testRealm, restored.id).map { it.username })
            .containsExactly(memberName)
    }
}
