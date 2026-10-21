package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.addOrganization
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddOrganization
import de.klg71.keycloakmigration.keycloakapi.model.Organization
import de.klg71.keycloakmigration.keycloakapi.organizationByAlias
import de.klg71.keycloakmigration.keycloakapi.realmExistsById

class DeleteOrganizationAction(
    realm: String?,
    private val alias: String
) : Action(realm) {

    private lateinit var original: Organization
    private var linkedIdentityProviders: List<String> = emptyList()

    override fun execute() {
        if (!client.realmExistsById(realm()))
            throw MigrationException("Realm with id: ${realm()} does not exist!")

        original = client.organizationByAlias(alias, realm())
        linkedIdentityProviders = client.organizationIdentityProviders(realm(), original.id.toString())
            .map { it.alias }

        client.deleteOrganization(original.id, realm()).run {
            if (!isSuccessful())
                throw MigrationException(
                    "Failed to delete organization with alias: $alias in realm: ${realm()}. " +
                        "Status: ${status()}, Reason: ${reason()}"
                )
        }
    }

    /**
     * Recreates the organization and re-links its identity providers. Members can't be restored.
     */
    override fun undo() {
        client.addOrganization(
            realm(),
            AddOrganization(original.name, original.alias, original.redirectUrl, original.domains, original.attributes)
        )
        val recreated = client.organizationByAlias(alias, realm())
        linkedIdentityProviders.forEach {
            client.linkIdentityProviderToOrganization(
                realm = realm(),
                orgId = recreated.id.toString(),
                identityProviderAlias = it
            )
        }
    }

    override fun name() = "DeleteOrganizationAction $alias"
}
