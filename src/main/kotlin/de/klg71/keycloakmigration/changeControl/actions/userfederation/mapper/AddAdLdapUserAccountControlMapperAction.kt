package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.userAccountControlMapper
import de.klg71.keycloakmigration.rest.extractLocationUUID
import de.klg71.keycloakmigration.rest.userFederationByName
import java.util.UUID

class AddAdLdapUserAccountControlMapperAction(
        realm: String? = null,
        private val name: String,
        private val adName: String) : Action(realm) {

    private lateinit var mapperId: UUID

    override fun execute() {
        assertMapperIsCreatable(
                client, name, adName, realm())

        val userFederation = client.userFederationByName(adName, realm())
        mapperId = client.addUserFederationMapper(userAccountControlMapper(name, userFederation.id), realm())
                .extractLocationUUID()
    }

    override fun undo() {
        client.deleteUserFederationMapper(realm(), mapperId)
    }

    override fun name() = "AddAdLdapUserAccountControlMapper $name"

}
