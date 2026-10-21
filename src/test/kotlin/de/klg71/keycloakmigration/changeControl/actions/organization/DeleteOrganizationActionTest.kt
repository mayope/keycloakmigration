package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.RealmChecker
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.Organization
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationDomain
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationMember
import de.klg71.keycloakmigration.keycloakapi.model.Realm
import feign.Request
import feign.Response
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.UUID

class DeleteOrganizationActionTest : KoinTest {

    private val client = mockk<KeycloakClient>()
    private val realmChecker = mockk<RealmChecker>(relaxed = true)

    private val realmName = "test"
    private val alias = "managed-alias"
    private val organization = Organization(
        id = UUID.randomUUID(),
        name = "Managed Name",
        alias = alias,
        redirectUrl = null,
        domains = setOf(OrganizationDomain("managed-test.com"))
    )

    @Before
    fun setup() {
        startKoin {
            modules(module {
                single { client }
                single { realmChecker }
            })
        }
        every { client.realms() } returns listOf(mockk<Realm> {
            every { id } returns realmName
            every { realm } returns realmName
        })
        every { client.organizations(realmName) } returns listOf(organization)
        every { client.organization(realmName, organization.id) } returns organization
        every { client.organizationIdentityProviders(realmName, organization.id.toString()) } returns emptyList()
        every { client.organizationMembers(realmName, organization.id.toString(), 0, any()) } returns listOf(
            OrganizationMember(UUID.randomUUID(), "managed-user", "MANAGED"),
            OrganizationMember(UUID.randomUUID(), "unmanaged-user", "UNMANAGED")
        )
        every { client.deleteOrganization(organization.id, realmName) } returns noContent()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testRefusesToDeleteOrganizationWithManagedMembers() {
        assertThatThrownBy { DeleteOrganizationAction(realmName, alias).executeIt() }
            .isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("1 managed member(s)")

        verify(exactly = 0) { client.deleteOrganization(any(), any()) }
    }

    @Test
    fun testDeletesOrganizationWithManagedMembersWhenAllowed() {
        DeleteOrganizationAction(realmName, alias, deleteManagedMembers = true).executeIt()

        verify(exactly = 1) { client.deleteOrganization(organization.id, realmName) }
    }

    private fun noContent() = Response.builder()
        .status(204)
        .request(Request.create(Request.HttpMethod.DELETE, "/", emptyMap(), null, Charsets.UTF_8, null))
        .build()
}
