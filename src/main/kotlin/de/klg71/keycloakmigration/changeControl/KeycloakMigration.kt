package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.Attributes
import de.klg71.keycloakmigration.model.ChangeLog
import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.rest.KeycloakClient
import org.apache.commons.codec.digest.DigestUtils
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.slf4j.LoggerFactory
import java.util.*

class KeycloakMigration(private val migrationFile: String) : KoinComponent {
    private val yamlObjectMapper by inject<ObjectMapper>(name = "yamlObjectMapper")
    private val client by inject<KeycloakClient>()
    private val migrationUserId by inject<UUID>(name = "migrationUserId")

    private val loader = Thread.currentThread().contextClassLoader!!
    private val changeHashes = getMigrationsHashes()

    companion object {
        val LOG = LoggerFactory.getLogger(KeycloakMigration::class.java)!!
    }

    init {
        try {
            changesTodo().apply {
                forEach { change ->
                    LOG.info("Executing change: ${change.id}:${change.author}")
                    doChange(change)
                }
            }.let {
                writeChangesToUser(it)
            }
        } catch (e:Throwable){
            LOG.info("Migration were unsuccessful see errors above!",e)
        }

    }

    private fun doChange(change: ChangeSet) {
        mutableListOf<Action>().run {
            try {
                change.changes.forEach { action ->
                    action.executeIt()
                    add(action)
                }

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
        changes.forEach { builder.append(it.hash()) }
        builder.toString()
    }.let {
        DigestUtils.sha256Hex(it)
    }!!

    private fun changesTodo():List<ChangeSet> =
            changes(migrationFile).apply {
                changeHashes.forEachIndexed { i, it ->
                    if (get(i).hash() != it) {
                        throw MigrationException("Invalid hash expected: $it got ${get(i).hash()}")
                    }
                    LOG.info("Skipping migration: ${get(i).id}")
                }
            }.run {
                subList(changeHashes.size , size )
            }

    private fun MutableList<Action>.rollback() {
        reverse()
        forEach {
            it.undoIt()
        }
    }

    private fun changes(fileName: String): List<ChangeSet> =
            readYamlFile<ChangeLog>(fileName).includes.map {
                readYamlFile<ChangeSet>(it.path)
            }

    private inline fun <reified T> readYamlFile(fileName: String): T =
            yamlObjectMapper.readValue(loader.getResourceAsStream(fileName)
                    ?: throw RuntimeException("File $fileName not found."))

    private fun writeChangesToUser(changes: List<ChangeSet>) {
        client.user(migrationUserId, "master").run {
            userAttributes().run {
                addMigrations(changes)
            }.let {
                client.updateUser(id, User(id, createdTimestamp, username, enabled, emailVerified, it,
                        notBefore, totp, access, disableableCredentialTypes, requiredActions, email, firstName, lastName), "master")
            }
        }
    }

    private fun Attributes.addMigrations(changes: List<ChangeSet>): Attributes = toMutableMap().apply {
        put("migrations", migrations().addChangeHashes(changes))
    }

    private fun List<String>.addChangeHashes(changes: List<ChangeSet>) =
            toMutableList().apply {
                changes.forEach { change ->
                    add(change.hash())
                }
            }

    private fun User.userAttributes() = attributes ?: emptyMap()

    private fun Attributes.migrations() = get("migrations") ?: emptyList()

    private fun getMigrationsHashes(): List<String> =
            client.user(migrationUserId, "master").run {
                if (Objects.isNull(attributes)) {
                    return emptyList()
                }
                if ("migrations" !in attributes!!) {
                    return emptyList()
                }
                return attributes["migrations"]!!
            }
}