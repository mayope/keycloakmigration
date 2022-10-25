package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProviderMapper
import de.klg71.keycloakmigration.keycloakapi.model.roleMapper

internal class AddSamlRoleMapperAction(
    realm: String? = null,
    private val identityProviderAlias: String,
    private val name: String,
    private val attributeValue: String,
    private val role: String
) : Action(realm) {

    override fun execute() {
        assertMapperIsCreatable(client, name, identityProviderAlias, realm())
        client.addIdentityProviderMapper(addIdentityProviderMapper(), realm(), identityProviderAlias).apply {
            if (!isSuccessful()) {
                throw KeycloakApiException(this.body().asReader().readText())
            }
        }
    }

    private fun addIdentityProviderMapper(): AddIdentityProviderMapper =
        roleMapper(identityProviderAlias, name, attributeValue, role)

    override fun undo() {
        client.identityProviderMappers(realm(), identityProviderAlias).find {
            it.name == name
        }?.let {
            client.deleteIdentityProviderMapper(realm(), identityProviderAlias, name)
        }
    }

    override fun name() = "AddSamlRoleMapper $name"

}
