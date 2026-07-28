package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationDomain
import de.klg71.keycloakmigration.keycloakapi.model.UpdateOrganization
import de.klg71.keycloakmigration.keycloakapi.organizationByAlias
import de.klg71.keycloakmigration.keycloakapi.realmExistsById
import de.klg71.keycloakmigration.keycloakapi.editOrganization
import de.klg71.keycloakmigration.keycloakapi.model.Organization

class UpdateOrganizationAction(
    realm: String?,
    private val alias: String,
    private val name: String?,
    private val redirectUrl: String? = null,
    private var domains: Set<OrganizationDomain>?,
    private val attributes: Map<String, List<String>>?
) : Action(realm) {

    private lateinit var original: Organization

    override fun execute() {
        if (!client.realmExistsById(realm()))
            throw MigrationException("Realm with id: ${realm()} does not exist!")

        domains?.let {
            if (it.isEmpty())
                throw MigrationException("At least one domain needs to be provided!")
        }

        original = client.organizationByAlias(alias, realm())

        val updatedOrganization = UpdateOrganization(
            alias,
            name = name ?: original.name,
            redirectUrl = redirectUrl ?: original.redirectUrl,
            domains = domains ?: original.domains,
            attributes = attributes ?: original.attributes
        )

        client.editOrganization(realm(), original.id, updatedOrganization)
    }

    override fun undo() {
        val originalOrganization = UpdateOrganization(
            alias,
            name = original.name,
            redirectUrl = original.redirectUrl,
            domains = original.domains,
            attributes = original.attributes
        )
        client.updateOrganization(realm(), original.id, originalOrganization)
    }

    override fun name() = "UpdateOrganizationAction $name"
}
