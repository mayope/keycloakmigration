package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.*
import de.klg71.keycloakmigration.keycloakapi.organizationByName
import de.klg71.keycloakmigration.keycloakapi.realmExistsById

data class OrganizationDomain(
    val name: String,
    val verified: Boolean?
)

class AddOrganizationAction(
    realm: String?,
    private val name: String,
    private val description: String?,
    private val domains: List<OrganizationDomain>
) : Action(realm) {

    override fun execute() {
        if (!client.realmExistsById(realm())) {
            throw MigrationException("Realm with id: ${realm()} does not exist!")
        }

        val organization = AddOrganization(
            name,
            description ?: "",
            domains.map {
                Domain(it.name, it.verified ?: false)
            },
            emptyMap()
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
