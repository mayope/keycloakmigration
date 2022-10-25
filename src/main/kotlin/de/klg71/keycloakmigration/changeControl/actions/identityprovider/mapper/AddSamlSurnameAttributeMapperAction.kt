package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProviderMapper
import de.klg71.keycloakmigration.keycloakapi.model.surnameMapper

internal class AddSamlSurnameAttributeMapperAction(
    realm: String? = null,
    private val identityProviderAlias: String,
    private val name: String,
    private val attributeName: String
) : Action(realm) {

    override fun execute() {
        assertMapperIsCreatable(client, this.name(), identityProviderAlias, realm())
        client.addIdentityProviderMapper(addIdentityProviderMapper(), realm(), identityProviderAlias).apply {
            if (!isSuccessful()) {
                throw KeycloakApiException(this.body().asReader().readText())
            }
        }
    }

    private fun addIdentityProviderMapper(): AddIdentityProviderMapper =
        surnameMapper(identityProviderAlias, name, attributeName)

    override fun undo() {
        client.identityProviderMappers(realm(), identityProviderAlias).find {
            it.name == name
        }?.let {
            client.deleteIdentityProviderMapper(realm(), identityProviderAlias, name)
        }
    }

    override fun name() = "AddSamlSurnameAttributeMapper $name"

}
