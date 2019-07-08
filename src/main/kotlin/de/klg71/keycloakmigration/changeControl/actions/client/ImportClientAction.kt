package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.rest.extractLocationUUID
import org.apache.commons.codec.digest.DigestUtils
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*

class ImportClientAction(
        private val realm: String,
        private val clientRepresentationJsonFilename: String,
        private val relativeToFile: Boolean = true) : Action() {
    private lateinit var clientUuid: UUID

    private fun readJsonContent() =
            if (relativeToFile) {
                FileInputStream(Paths.get(path, clientRepresentationJsonFilename).toString()).bufferedReader().use { it.readText() }
            } else {
                FileInputStream(clientRepresentationJsonFilename).bufferedReader().use { it.readText() }
            }

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(readJsonContent())
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = calculateHash()


    override fun execute() {
        client.importClient(readJsonContent(), realm).run {
            clientUuid = extractLocationUUID()
        }
    }

    override fun undo() {
        client.deleteClient(clientUuid, realm)
    }

    override fun name() = "ImportClient $clientRepresentationJsonFilename"

}