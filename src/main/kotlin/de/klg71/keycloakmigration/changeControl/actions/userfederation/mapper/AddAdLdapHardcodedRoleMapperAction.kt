package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.hardcodedRoleMapper
import de.klg71.keycloakmigration.rest.extractLocationUUID
import de.klg71.keycloakmigration.rest.roleExistsByName
import de.klg71.keycloakmigration.rest.userFederationByName
import java.util.UUID

class AddAdLdapHardcodedRoleMapperAction(
        realm: String? = null,
        private val name: String,
        private val adName: String,
        private val role: String) : Action(realm) {

    private lateinit var mapperId: UUID

    override fun execute() {
        assertMapperIsCreatable(
                client, name, adName, realm())

        if (!client.roleExistsByName(role, realm())) {
            throw MigrationException("Realm role with name: $role does not exist in realm: ${realm()}!")
        }
        val userFederation = client.userFederationByName(adName, realm())
        mapperId = client.addUserFederationMapper(hardcodedRoleMapper(name, role, userFederation.id), realm())
                .extractLocationUUID()
    }

    override fun undo() {
        client.deleteUserFederationMapper(realm(), mapperId)
    }

    override fun name() = "AddAdLdapHardcodedRoleMapper $name"

}
