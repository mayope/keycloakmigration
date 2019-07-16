package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.Attributes
import de.klg71.keycloakmigration.model.ChangeLog
import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.rest.KeycloakClient
import org.apache.commons.codec.digest.DigestUtils.sha256Hex
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*
import java.util.Objects.isNull

class KeycloakMigrationExecute(private val migrationFile: String, private val realm: String) : KoinComponent {
    private val yamlObjectMapper by inject<ObjectMapper>(name = "yamlObjectMapper")
    private val client by inject<KeycloakClient>()
    private val migrationUserId by inject<UUID>(name = "migrationUserId")

    private val changeHashes = getMigrationsHashes()

    companion object {
        val LOG = LoggerFactory.getLogger(KeycloakMigrationExecute::class.java)!!
    }

    init {
        try {
            changesTodo().forEach { change ->
                LOG.info("Executing change: ${change.id}:${change.author}")
                doChange(change)
            }
        } catch (e: Throwable) {
            LOG.error("Migration were unsuccessful see errors above!", e)
        }

    }

    private fun doChange(change: ChangeSet) {
        mutableListOf<Action>().run {
            try {
                change.changes.forEach { action ->
                    action.executeIt()
                    add(action)
                }

                writeChangesToUser(change)
                LOG.info("Migration ${change.id}:${change.author} Successful executed: $size actions.")
            } catch (e: Exception) {
                LOG.error("Error occurred while migrating: ${e.message} ", e)
                LOG.error("Rolling back changes")
                rollback()
                throw e
            }
        }
    }

    private fun ChangeSet.hash() = StringBuilder().let { builder ->
        builder.append(author)
        builder.append(id)
        changes.forEach {
            it.path = path
            builder.append(it.hash())
        }
        builder.toString()
    }.let {
        sha256Hex(it)
    }!!

    private fun changesTodo(): List<ChangeSet> =
            changes(migrationFile).apply {
                changeHashes.forEachIndexed { i, it ->
                    if (get(i).hash() != it) {
                        throw MigrationException("Invalid hash expected: $it got ${get(i).hash()}")
                    }
                    LOG.info("Skipping migration: ${get(i).id}")
                }
            }.run {
                subList(changeHashes.size, size)
            }

    private fun MutableList<Action>.rollback() {
        reverse()
        forEach {
            it.undoIt()
        }
    }

    private fun changes(fileName: String): List<ChangeSet> =
            readYamlFile<ChangeLog>(fileName).includes.map {
                if (it.relativeToFile) {
                    readYamlFile<ChangeSet>(parentPath(fileName, it.path).toString()).apply {
                        path = parentPath(fileName, it.path).parent.toString()
                        changes.forEach { action ->
                            action.path = path
                        }
                    }
                } else {
                    readYamlFile<ChangeSet>(it.path).apply {
                        path = File(it.path).absoluteFile.parent.toString()
                        changes.forEach { action ->
                            action.path = path
                        }
                    }
                }
            }

    private fun parentPath(fileName: String, path: String) =
            Paths.get(File(fileName).absoluteFile.parentFile.absolutePath, path)

    private inline fun <reified T> readYamlFile(fileName: String): T {
        if (!File(fileName).exists()) {
            throw RuntimeException("File $fileName not found.")
        }
        return yamlObjectMapper.readValue(FileInputStream(fileName))
    }

    private fun writeChangesToUser(changes: ChangeSet) {
        client.user(migrationUserId, realm).run {
            userAttributes().run {
                addMigration(changes)
            }.let {
                client.updateUser(id, User(id, createdTimestamp, username, enabled, emailVerified, it,
                        notBefore, totp, access, disableableCredentialTypes, requiredActions, email, firstName, lastName, null), realm)
            }
        }
    }

    private fun Attributes.addMigration(change: ChangeSet): Attributes = toMutableMap().apply {
        put("migrations", migrations().addChangeHash(change))
    }

    private fun List<String>.addChangeHash(change: ChangeSet) =
            toMutableList().apply {
                add(change.hash())
            }

    private fun User.userAttributes() = attributes ?: emptyMap()

    private fun Attributes.migrations() = get("migrations") ?: emptyList()

    private fun getMigrationsHashes(): List<String> =
            client.user(migrationUserId, realm).run {
                if (isNull(attributes)) {
                    return emptyList()
                }
                if ("migrations" !in attributes!!) {
                    return emptyList()
                }
                return attributes["migrations"]!!
            }
}