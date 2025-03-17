package de.klg71.keycloakmigration.changeControl.actions.client.authz

import de.klg71.keycloakmigration.changeControl.StringEnvSubstitutor
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.clientById
import de.klg71.keycloakmigration.keycloakapi.existsClient
import de.klg71.keycloakmigration.keycloakapi.isClientAuthorizationEnabled
import de.klg71.keycloakmigration.keycloakapi.model.ImportAuthorizationRepresentation
import org.koin.core.component.inject
import java.io.FileInputStream
import java.nio.file.Paths

class ImportClientAuthorizationAction(
    realm: String? = null,
    private val clientId: String,
    private val authorizationRepresentationJsonFilename: String,
    private val relativeToFile: Boolean = true) : Action(realm) {
    private val stringEnvSubstitutor by inject<StringEnvSubstitutor>()

    private fun fileBufferedReader() =
        if (relativeToFile) {
            FileInputStream(Paths.get(path, authorizationRepresentationJsonFilename).toString()).bufferedReader()
        } else {
            FileInputStream(authorizationRepresentationJsonFilename).bufferedReader()
        }

    private fun readJsonContentWithWhitespace() = fileBufferedReader().use { it.readText() }.let {
        stringEnvSubstitutor.substituteParameters(it)
    }

    override fun execute() {
        if (!client.existsClient(clientId, realm())) {
            throw MigrationException("Client with id: $clientId does not exist in realm: $realm!")
        }
        if (!client.isClientAuthorizationEnabled(clientId, realm())) {
            throw MigrationException("Client with id: $clientId does not have authorization enabled!")
        }

        client.clientById(clientId, realm()).let {
            client.importAuthorization(
                ImportAuthorizationRepresentation(readJsonContentWithWhitespace()), it.id, realm()
            )
        }
    }

    override fun undo() {
        throw MigrationException("ImportClientAuthorization cannot be undone.")
    }

    override fun name() = "ImportClientAuthorization $authorizationRepresentationJsonFilename"

}
