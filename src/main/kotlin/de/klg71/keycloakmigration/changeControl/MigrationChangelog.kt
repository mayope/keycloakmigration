package de.klg71.keycloakmigration.changeControl

import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.Attributes
import de.klg71.keycloakmigration.keycloakapi.model.User
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import java.util.UUID

private const val CURRENT_CHANGELOG_VERSION = "v2"

/**
 * Service keeping track of the already executed migrations and which new migrations should be executed
 */
@Suppress("TooManyFunctions")
internal class MigrationChangelog(private val migrationUserId: UUID, private val realm: String) : KoinComponent {

    private val client by inject<KeycloakClient>()

    companion object {
        val LOG = LoggerFactory.getLogger(MigrationChangelog::class.java)!!
        const val migrationAttributeName = "migrations"
    }

    /**
     * Calculate which Changeset should be executed and which are already done depending on information in keycloak
     */
    internal fun changesTodo(changes: List<ChangeSet>, correctHashes: Boolean = false): List<ChangeSet> {
        val relevantChanges = changes.filterDisabled()

        migrateChangeLogToV2(relevantChanges.map { it.hash() })
        val changeHashes = getMigrationsHashes()

        return relevantChanges.apply {
            checkExistingHashes(changeHashes, correctHashes)
        }.run {
            subList(changeHashes.size, size)
        }
    }

    private fun List<ChangeSet>.checkExistingHashes(changeHashes: List<String>, correctHashes: Boolean) {
        changeHashes.forEachIndexed { i, it ->
            if (get(i).hash() != it) {
                handleHashError(correctHashes, it, i)
            }
            LOG.info("Skipping migration: ${get(i).id}")
        }
    }

    private fun List<ChangeSet>.filterDisabled(): List<ChangeSet> {
        return filter {
            if (!it.enabled) LOG.info("Skipping disabled migration: ${it.id}")
            it.enabled
        }
    }

    private fun List<ChangeSet>.handleHashError(
        correctHashes: Boolean, remoteHash: String, migrationIndex: Int) {
        if (!correctHashes) {
            throw MigrationException(
                "Invalid hash expected: $remoteHash (remote) " +
                        "got ${get(migrationIndex).hash()} (local) in migration: ${get(migrationIndex).id}"
            )
        }
        formatHashV2(migrationIndex, get(migrationIndex).hash).let {
            replaceHash(remoteHash, it)
            LOG.warn("Replaced hash: $remoteHash with $it for migration ${get(migrationIndex).id}")
        }
    }

    /**
     * Write the information about an executed changeSet to keycloak
     */
    internal fun writeChangeToUser(change: ChangeSet) {
        client.user(migrationUserId, realm).run {
            userAttributes().run {
                addMigration(change)
            }.let {
                client.updateUser(
                    id, User(
                        id, createdTimestamp, username, enabled, emailVerified, it,
                        notBefore, totp, access, disableableCredentialTypes, requiredActions, email, firstName,
                        lastName, null
                    ), realm
                )
            }
        }
    }

    private fun replaceHash(oldHash: String, newHash: String) {
        client.user(migrationUserId, realm).run {
            userAttributes().run {
                toMutableMap().apply {
                    put(migrationAttributeName, migrations().replaceString(oldHash, newHash))
                }
            }.let {
                client.updateUser(
                    id, User(
                        id, createdTimestamp, username, enabled, emailVerified, it,
                        notBefore, totp, access, disableableCredentialTypes, requiredActions, email, firstName,
                        lastName, null
                    ), realm
                )
            }
        }
    }

    private fun List<String>.replaceString(oldHash: String, newHash: String): List<String> {
        return map {
            if (it.contains(oldHash)) {
                newHash
            } else {
                it
            }
        }
    }

    private fun Attributes.addMigration(change: ChangeSet): Attributes = toMutableMap().apply {
        put(migrationAttributeName, migrations().addChangeHash(change, nextIndex()))
    }

    private fun nextIndex() = parse(migrationHashAttributes()).maxByOrNull { it.order }?.let {
        it.order + 1
    } ?: 0

    private fun List<String>.addChangeHash(change: ChangeSet, index: Int) =
        toMutableList().apply {
            add(formatHashV2(index, change.hash()))
        }

    private fun User.userAttributes() = attributes ?: emptyMap()

    private fun Attributes.migrations() = get(migrationAttributeName) ?: emptyList()

    /**
     * old format were just the migration hashes, but keycloak doesn't reliably save the hash order.
     * So we need to implement a new format: v2/#order/#hash
     *
     * We need the currently relevant hashes to fix any wrong ordering
     */
    @Suppress("ReturnCount")
    private fun getMigrationsHashes(): List<String> {
        return parse(migrationHashAttributes()).sortedBy { it.order }.map { it.hash }
    }

    private fun parse(hashAttributes: List<String>): List<MigrationEntity> {
        return hashAttributes.map {
            val (version, order, hash) = it.split("/")
            if (version != CURRENT_CHANGELOG_VERSION) {
                throw MigrationException(
                    "Unknown changelog version: $version detected, expected version: $CURRENT_CHANGELOG_VERSION."
                )
            }
            MigrationEntity(version, order.toInt(), hash)
        }
    }

    private data class MigrationEntity(val version: String, val order: Int, val hash: String)

    private fun migrateChangeLogToV2(existingHashes: List<String>) {
        migrationHashAttributes().let {
            if (it.isEmpty()) {
                return
            }
            if (it.first().contains("v2/")) {
                return
            }
            if (!existingHashes.containsAll(it)) {
                throw MigrationException("Wrong changelog encountered, while trying to migrate hashes to v2.")
            }

            LOG.info("Reordering hashes in keycloak by existing hashes")
            migrateToV2(it.sortedBy { existingHashes.indexOf(it) })
        }
    }

    private fun migrateToV2(v1Hashes: List<String>) {
        LOG.info("Migrating your changelog to v2 format")
        v1Hashes.forEachIndexed { index, hash ->
            replaceHash(hash, formatHashV2(index, hash))
        }
        LOG.info("Migrated your changelog to v2 format")
    }

    private fun formatHashV2(index: Int, hash: String) = "v2/$index/$hash"

    private fun migrationHashAttributes(): List<String> {
        client.user(migrationUserId, realm).run {
            if (attributesNullOrEmpty()) {
                return emptyList()
            }
            return attributes!![migrationAttributeName]!!
        }
    }

    private fun User.attributesNullOrEmpty() = attributes == null || migrationAttributeName !in attributes!!

}

