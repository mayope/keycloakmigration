package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.userAttributeMapper
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import java.util.UUID

class AddAdLdapUserAttributeMapperAction(
        realm: String? = null,
        private val name: String,
        private val adName: String,
        private val userModelAttribute: String,
        private val ldapAttribute: String,
        private val readOnly: Boolean = false,
        private val alwaysReadFromLdap: Boolean = false,
        private val isMandatoryInLdap: Boolean = false
) : Action(realm) {

    private lateinit var mapperId: UUID

    override fun execute() {
        assertMapperIsCreatable(
                client, name, adName, realm())

        val userFederation = client.userFederationByName(adName, realm())
        mapperId = client.addUserFederationMapper(
                userAttributeMapper(name, userFederation.id, userModelAttribute, ldapAttribute, readOnly,
                        alwaysReadFromLdap, isMandatoryInLdap), realm()).extractLocationUUID()
    }

    override fun undo() {
        client.deleteUserFederationMapper(realm(), mapperId)
    }

    override fun name() = "AddAdLdapUserAttributeMapper $name"

}
