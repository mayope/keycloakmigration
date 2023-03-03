package de.klg71.keycloakmigration.changeControl.actions.identityprovider.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.identityProviderByAlias
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProviderMapper

internal fun assertSamlMapperIsCreatable(client: KeycloakClient,
    name: String,
    identityProviderAlias: String,
    realm: String) {
    assertIdentityProviderMapperIsCreatable(client, name, identityProviderAlias, realm)
    val identityProvider = client.identityProviderByAlias(identityProviderAlias, realm)
    if (identityProvider.providerId != "saml") {
        throw MigrationException(
            "Unsupported type: ${identityProvider.providerId} of IdentityProvider with name:" +
                    " $identityProviderAlias in: $realm for ${object {}.javaClass.enclosingClass.simpleName}!"
        )
    }
}

internal class AddSamlMapperAction(
    realm: String? = null,
    val config: Map<String, String>,
    val identityProviderAlias: String,
    val identityProviderMapper: String,
    val name: String
) : Action(realm) {

    override fun execute() {
        assertSamlMapperIsCreatable(client, name, identityProviderAlias, realm())
        client.addIdentityProviderMapper(addIdentityProviderMapper(), realm(), identityProviderAlias).apply {
            if (!isSuccessful()) {
                throw KeycloakApiException(body().asReader().readText(),status())
            }
        }
    }

    private fun addIdentityProviderMapper(): AddIdentityProviderMapper =
        AddIdentityProviderMapper(config, identityProviderAlias, identityProviderMapper, name)

    override fun undo() {
        client.identityProviderMappers(realm(), identityProviderAlias).find {
            it.name == name
        }?.let {
            client.deleteIdentityProviderMapper(realm(), identityProviderAlias, it.id)
        }
    }

    override fun name() = "AddSamlMapper $name to $identityProviderAlias"

}
