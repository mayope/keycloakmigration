package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.AddOrganization
import de.klg71.keycloakmigration.keycloakapi.model.OrganizationDomain
import de.klg71.keycloakmigration.keycloakapi.organizationByName
import de.klg71.keycloakmigration.keycloakapi.realmExistsById
import java.nio.charset.StandardCharsets

class AddOrganizationAction(
    realm: String?,
    private val name: String,
    private val alias: String? = name,
    private val redirectUrl: String? = null,
    private val domains: Set<OrganizationDomain>,
    private val attributes: Map<String, List<String>>? = mapOf()
) : Action(realm) {

    override fun execute() {
        if (!client.realmExistsById(realm()))
            throw MigrationException("Realm with id: ${realm()} does not exist!")

        if (client.organizations(realm()).any { it.name == name })
            throw MigrationException("Organisation with name: $name already exists!")

        if (domains.isEmpty())
            throw MigrationException("At least one domain needs to be provided!")

        val organization = AddOrganization(
            name, alias, redirectUrl, domains, attributes
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
