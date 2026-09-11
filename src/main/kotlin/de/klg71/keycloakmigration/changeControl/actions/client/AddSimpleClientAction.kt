package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.AddSimpleClient
import de.klg71.keycloakmigration.keycloakapi.clientById
import de.klg71.keycloakmigration.keycloakapi.clientUUID
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.model.OPENID_CONNECT_PROTOCOL
import java.net.HttpURLConnection.HTTP_CONFLICT
import java.util.*

@Suppress("LongParameterList")
class AddSimpleClientAction(
        realm: String?,
        private val clientId: String,
        private val enabled: Boolean = true,
        private val attributes: Map<String, String> = mapOf(),
        private val protocol: String = OPENID_CONNECT_PROTOCOL,
        private val secret: String? = null,
        private val authorizationServicesEnabled: Boolean = false,
        private var serviceAccountsEnabled: Boolean = false,
        private val publicClient: Boolean = true,
        private val redirectUris: List<String> = emptyList()) : Action(realm) {

    private lateinit var clientUuid: UUID

    // True only when execute() actually created the client. Adopting a
    // pre-existing client (409) must NOT set this, so undo() never deletes a
    // client this action did not create.
    private var createdByThisAction = false

    private val addClient = addClient()

    private fun addClient() = AddSimpleClient(
        clientId,
        enabled,
        attributes,
        protocol,
        redirectUris,
        secret,
        authorizationServicesEnabled,
        serviceAccountsEnabled,
        publicClient
    )

    override fun execute() {
        client.addSimpleClient(addClient, realm()).run {
            clientUuid = when {
                isSuccessful() -> {
                    createdByThisAction = true
                    extractLocationUUID()
                }
                // The client already exists. This happens when a previous run
                // created it but was interrupted before committing the changeset
                // to the migration ledger, so the changeset is replayed and the
                // add is re-attempted. Adopt the existing client and let the
                // changeset continue and converge, instead of failing forever.
                // Keyed on the exact 409 from Keycloak, never a pre-check, so any
                // other error (400/403/500) is still raised by extractLocationUUID.
                status() == HTTP_CONFLICT -> {
                    LOG.info(
                        "Client $clientId already exists in realm ${realm()}, " +
                            "adopting the existing client (idempotent add)."
                    )
                    client.clientUUID(clientId, realm())
                }
                else -> extractLocationUUID()
            }
        }
    }

    override fun undo() {
        // Only delete the client if this action created it. A client that was
        // adopted (already existed) predates this action and must be left intact.
        if (!createdByThisAction) {
            return
        }
        client.clientById(clientId, realm()).run {
            client.deleteClient(id, realm())
        }
    }

    override fun name() = "AddSimpleClient $clientId"

}
