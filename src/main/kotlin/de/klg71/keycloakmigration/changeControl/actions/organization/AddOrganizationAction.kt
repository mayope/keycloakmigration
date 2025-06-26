package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.AddOrganization
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationDomain
import de.klg71.keycloakmigration.keycloakapi.organizationByName
import de.klg71.keycloakmigration.keycloakapi.realmExistsById

class AddOrganizationAction(
    realm: String?,
    private val name: String,
    private val alias: String? = name,
    private val redirectUrl: String? = null,
    private val domains: Set<OrganizationDomain>,
    private val config: Map<String, String>,
    ) : Action(realm) {

    override fun execute() {
        if (!client.realmExistsById(realm()))
            throw MigrationException("Realm with id: ${realm()} does not exist!")

        if (client.organizations(realm()).any { it.name == name })
            throw MigrationException("Organisation with name: $name already exists!")

        if (domains.isEmpty())
            throw MigrationException("At least one domain needs to be provided!")

        if (config.isEmpty())
            throw MigrationException("At least one theme needs to be provided!")

        val organization = AddOrganization(
            name, alias, redirectUrl, domains, config
        )

        client.addOrganization(realm(), organization)
    }

    override fun undo() {
        client.organizationByName(name, realm()).run {
            client.deleteOrganization(id, realm())
        }
    }

    override fun name() = "AddOrganizationAction $name"
}
