package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakApiException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.identityProviderByAlias
import de.klg71.keycloakmigration.keycloakapi.identityProviderExistsByAlias
import de.klg71.keycloakmigration.keycloakapi.identityProviderMapperExistsByName
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProviderMapper

internal fun assertMapperIsCreatable(client: KeycloakClient, name: String, identityProviderAlias: String, realm: String) {
    if (!client.identityProviderExistsByAlias(identityProviderAlias, realm)) {
        throw MigrationException("IdentityProvider with name: $identityProviderAlias does not exist in realm: $realm!")
    }
    if (client.identityProviderMapperExistsByName(identityProviderAlias, name, realm)) {
        throw MigrationException(
            "Mapper with name: $name already exists in IdentityProvider with name: $identityProviderAlias in realm: $realm!"
        )
    }
    val identityProvider = client.identityProviderByAlias(identityProviderAlias, realm)
    if (identityProvider.providerId != "saml") {
        throw MigrationException(
            "Unsupported type: ${identityProvider.providerId} of IdentityProvider with name: $identityProviderAlias in: $realm for ${object {}.javaClass.enclosingClass.simpleName}!"
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
        assertMapperIsCreatable(client, name, identityProviderAlias, realm())
        client.addIdentityProviderMapper(addIdentityProviderMapper(), realm(), identityProviderAlias).apply {
            if (!isSuccessful()) {
                throw KeycloakApiException(this.body().asReader().readText())
            }
        }
    }

    private fun addIdentityProviderMapper(): AddIdentityProviderMapper =
        AddIdentityProviderMapper(config, identityProviderAlias, identityProviderMapper, name)

    override fun undo() {
        client.identityProviderMappers(realm(), identityProviderAlias).find {
            it.name == name
        }?.let {
            client.deleteIdentityProviderMapper(realm(), identityProviderAlias, name)
        }
    }

    override fun name() = "AddSamlMapper $name"

}
