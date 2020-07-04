package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.StringEnvSubstitutor
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.model.ImportClientRepresentation
import org.koin.core.inject
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.UUID

class ImportClientAction(
        realm: String? = null,
        private val clientRepresentationJsonFilename: String,
        private val relativeToFile: Boolean = true) : Action(realm) {
    private lateinit var clientUuid: UUID
    private val stringEnvSubstitutor by inject<StringEnvSubstitutor>()

    private fun fileBufferedReader() =
            if (relativeToFile) {
                FileInputStream(Paths.get(path, clientRepresentationJsonFilename).toString()).bufferedReader()
            } else {
                FileInputStream(clientRepresentationJsonFilename).bufferedReader()
            }

    private fun readJsonContentWithWhitespace() = fileBufferedReader().use { it.readText() }.let {
        stringEnvSubstitutor.substituteParameters(it)
    }

    override fun execute() {
        client.importClient(ImportClientRepresentation(readJsonContentWithWhitespace()), realm()).run {
            clientUuid = extractLocationUUID()
        }
    }

    override fun undo() {
        client.deleteClient(clientUuid, realm())
    }

    override fun name() = "ImportClient $clientRepresentationJsonFilename"

}
