package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.identityProviderMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProviderMapper
import de.klg71.keycloakmigration.keycloakapi.model.emailAddressMapper

class AddSamlEmailAddressAttributeMapperAction(
        realm: String? = null,
        private val identityProviderAlias: String,
        private val name: String,
        private val attributeName: String
) : Action(realm) {

    override fun execute() {
        if (client.identityProviderMapperExistsByName(identityProviderAlias, name, realm())) {
            throw MigrationException("Identity Provider Mapper with name: $name already exists on Identity Provider: $identityProviderAlias in realm: ${realm()}!")
        }
        client.addIdentityProviderMapper(addIdentityProviderMapper(), realm(), identityProviderAlias).apply {
            if (!isSuccessful()) {
                throw KeycloakApiException(this.body().asReader().readText())
            }
        }
    }

    private fun addIdentityProviderMapper(): AddIdentityProviderMapper =
            emailAddressMapper(identityProviderAlias, name, attributeName)

    override fun undo() {
        client.identityProviderMappers(realm(), identityProviderAlias).find {
            it.name == name
        }?.let {
            client.deleteIdentityProviderMapper(realm(), identityProviderAlias, name)
        }
    }

    override fun name() = "AddIdentityProviderMapper $name"

}
