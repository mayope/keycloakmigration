package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.fullNameMapper
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import java.util.UUID

class AddAdLdapFullNameMapperAction(
        realm: String? = null,
        private val name: String,
        private val adName: String,
        private val ldapFullNameAttribute: String,
        private val readOnly: Boolean = true,
        private val writeOnly: Boolean = false) : Action(realm) {

    private lateinit var mapperId: UUID

    override fun execute() {
        assertMapperIsCreatable(
                client, name, adName, realm())

        val userFederation = client.userFederationByName(adName, realm())
        mapperId = client.addUserFederationMapper(
                fullNameMapper(name, userFederation.id, ldapFullNameAttribute, readOnly, writeOnly), realm())
                .extractLocationUUID()
    }

    override fun undo() {
        client.deleteUserFederationMapper(realm(), mapperId)
    }

    override fun name() = "AddAdLdapFullNameMapper $name"

}
