package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.model.ldapMapper
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.ldapMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import de.klg71.keycloakmigration.keycloakapi.userFederationExistsByName
import java.util.UUID

internal fun assertMapperIsCreatable(client:KeycloakClient, name:String, adName:String, realm:String) {
    if (!client.userFederationExistsByName(adName, realm)) {
        throw MigrationException("UserFederation with name: $adName does not exists in realm: ${realm}!")
    }
    if (client.ldapMapperExistsByName(adName, name, realm)) {
        throw MigrationException(
                "Mapper with name: $name already exists in UserFederation with name: $adName in realm: ${realm}!")
    }
}

class AddAdLdapMapperAction(
        realm: String? = null,
        private val name: String,
        private val adName:String,
        private val providerId:String,
        private val config: Map<String, String>) : Action(realm) {

    private lateinit var mapperId: UUID

    override fun execute() {
        assertMapperIsCreatable(
                client, name, adName, realm())

        val userFederation = client.userFederationByName(adName, realm())
        mapperId = client.addUserFederationMapper(ldapMapper(name, config, userFederation.id, providerId), realm())
                .extractLocationUUID()
    }


    override fun undo() {
        client.deleteUserFederationMapper(realm(),mapperId)
    }


    override fun name() = "AddLdapMapper $name"

}
