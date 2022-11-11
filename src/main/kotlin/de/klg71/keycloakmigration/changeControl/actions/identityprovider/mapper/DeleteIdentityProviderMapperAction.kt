package de.klg71.keycloakmigration.changeControl.actions.identityprovider.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProviderMapper
import de.klg71.keycloakmigration.keycloakapi.model.IdentityProviderMapper

open class DeleteIdentityProviderMapperAction(
    realm: String?,
    protected val identityProviderAlias: String,
    protected val name: String
) : Action(realm) {

    private var deletedMapper: IdentityProviderMapper? = null;

    override fun execute() {
        deletedMapper = client.identityProviderMappers(realm(), identityProviderAlias).firstOrNull { it.name == name }
        deletedMapper?.let {
            client.deleteIdentityProviderMapper(realm(), identityProviderAlias, it.id)
        }
    }

    override fun undo() {
        deletedMapper?.let {
            addIdentityProviderMapper(client, AddIdentityProviderMapper.from(it), identityProviderAlias, name, realm())
        }
    }

    override fun name() = "DeleteIdentityProviderMapper $name from $identityProviderAlias"

}
