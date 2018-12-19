package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.model.Attributes
import de.klg71.keycloakmigration.model.User
import de.klg71.keycloakmigration.rest.KeycloakClient
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.slf4j.LoggerFactory
import java.util.*

class KeycloakMigration : KoinComponent {
    private val yamlObjectMapper by inject<ObjectMapper>(name = "yamlObjectMapper")
    private val client by inject<KeycloakClient>()
    val migrationUserId by inject<UUID>(name = "migrationUserId")

    companion object {
        val LOG = LoggerFactory.getLogger(KeycloakMigration::class.java)!!
    }

    init {
        val loader = Thread.currentThread().contextClassLoader
        val set = yamlObjectMapper.readValue<ChangeFile>(loader.getResourceAsStream("testMigration/changesets/initial.yml"))

        mutableListOf<Action>().run {
            try {
                val actionHashes = getMigrationsHashes()
                set.keycloakChangeSet.changes.forEachIndexed { i, it ->
                    if (i >= actionHashes.size) {
                        LOG.info("Executing Migration: ${it.name()}")
                        it.executeIt()
                        add(it)
                    } else if (it.hash() == actionHashes[i]) {
                        LOG.info("Skipping Migration: ${it.name()}")
                    } else {
                        throw MigrationException("Invalid hash expected: ${actionHashes[i]} got ${it.hash()}")
                    }
                }
                writeMigrationsToUser(this)
            } catch (e: Exception) {
                LOG.error("Error occurred while migrating: ${e.message} ", e)
                LOG.error("Rolling back changes")
                reverse()
                forEach {
                    it.undoIt()
                }
            }
        }

    }

    private fun writeMigrationsToUser(actions: List<Action>) {
        client.user(migrationUserId, "master").run {
            userAttributes().run {
                addMigrations(actions)
            }.let {
                client.updateUser(id, User(id, createdTimestamp, username, enabled, emailVerified, it,
                        notBefore, totp, access, disableableCredentialTypes, requiredActions, email, firstName, lastName), "master")
            }
        }
    }

    private fun Attributes.addMigrations(actions: List<Action>): Attributes = toMutableMap().apply {
        put("migrations", migrations().addActionHashes(actions))
    }

    private fun List<String>.addActionHashes(actions: List<Action>) =
            toMutableList().apply {
                actions.forEach { action ->
                    add(action.hash())
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