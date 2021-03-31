package de.klg71.keycloakmigration.changeControl.actions.identityprovider

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.identityProviderExistsByAlias
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProvider
import de.klg71.keycloakmigration.keycloakapi.model.IdentityProvider
import org.slf4j.LoggerFactory

class DeleteIdentityProviderAction(
    realm: String? = null,
    private val alias: String,
) : Action(realm) {
    private val logger = LoggerFactory.getLogger(DeleteIdentityProviderAction::class.java)

    private var oldIdentityProvider: IdentityProvider? = null

    override fun execute() {
        if (!client.identityProviderExistsByAlias(alias, realm())) {
            return
        }
        oldIdentityProvider = client.identityProvider(realm(), alias)
        client.deleteIdentityProvider(realm(), alias)
    }

    override fun undo() {
        oldIdentityProvider?.let {
            client.addIdentityProvider(
                AddIdentityProvider(
                    it.providerId,
                    it.alias,
                    it.displayName,
                    it.enabled,
                    it.config,
                    it.trustEmail,
                    it.storeToken,
                    it.linkOnly,
                    it.firstBrokerLoginFlowAlias,
                    it.postBrokerLoginFlowAlias,
                    it.updateProfileFirstLoginMode
                ), realm()
            )
            logger.warn("Readded deleted IdentityProvider: {}, you have to reset the clientSecret", alias)
        }
    }


    override fun name() = "AddIdentityProvider $alias"

}
