package de.klg71.keycloakmigration.changeControl.actions.client

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.rest.extractLocationUUID
import org.apache.commons.codec.digest.DigestUtils
import java.io.FileInputStream
import java.util.*

class ImportClientAction(
        private val realm: String,
        private val clientRepresentationJsonFilename: String) : Action() {
    private val loader = Thread.currentThread().contextClassLoader!!

    private lateinit var clientUuid: UUID

    private val clientImport = readJsonContent();


    private fun readJsonContent() =
            FileInputStream(clientRepresentationJsonFilename).bufferedReader().use { it.readText() }

    private val hash = calculateHash()

    private fun calculateHash() =
            StringBuilder().run {
                append(realm)
                append(clientImport)
                toString()
            }.let {
                DigestUtils.sha256Hex(it)
            }!!

    override fun hash() = hash


    override fun execute() {
        client.importClient(clientImport, realm).run {
            clientUuid = extractLocationUUID()
        }
    }

    override fun undo() {
        client.deleteClient(clientUuid, realm)
    }

    override fun name() = "ImportClient $clientRepresentationJsonFilename"

}