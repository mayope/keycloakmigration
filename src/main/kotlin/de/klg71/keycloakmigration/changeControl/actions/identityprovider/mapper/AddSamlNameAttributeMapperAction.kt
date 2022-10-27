package de.klg71.keycloakmigration.changeControl.actions.identityprovider.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProviderMapper
import de.klg71.keycloakmigration.keycloakapi.model.nameMapper

internal class AddSamlNameAttributeMapperAction(
    realm: String? = null,
    private val identityProviderAlias: String,
    private val name: String,
    private val attributeName: String
) : Action(realm) {

    override fun execute() {
        assertSamlMapperIsCreatable(client, name, identityProviderAlias, realm())
        client.addIdentityProviderMapper(addIdentityProviderMapper(), realm(), identityProviderAlias).apply {
            if (!isSuccessful()) {
                throw KeycloakApiException(this.body().asReader().readText())
            }
        }
    }

    private fun addIdentityProviderMapper(): AddIdentityProviderMapper =
        nameMapper(identityProviderAlias, name, attributeName)

    override fun undo() {
        client.identityProviderMappers(realm(), identityProviderAlias).find {
            it.name == name
        }?.let {
            client.deleteIdentityProviderMapper(realm(), identityProviderAlias, it.id)
        }
    }

    override fun name() = "AddSamlNameAttributeMapper $name"

}
