package de.klg71.keycloakmigration.changeControl

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.ChangeSet
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Execute the keycloakmigration
 */
internal class KeycloakMigration(private val migrationFile: String, realm: String) : KoinComponent {
    private val migrationUserId by inject<UUID>(named("migrationUserId"))
    private val changeFileReader = ChangeFileReader()
    private val changelog = MigrationChangelog(migrationUserId, realm)

    companion object {
        val LOG = LoggerFactory.getLogger(KeycloakMigration::class.java)!!
    }

    internal fun execute() {
        try {
            changeFileReader.changes(migrationFile).let {
                changelog.changesTodo(it)
            }.forEach { change ->
                LOG.info("Executing change: ${change.id}:${change.author}")
                doChange(change)
            }
        } catch (e: Throwable) {
            LOG.error("Migrations were unsuccessful:", e)
            throw e
        }

    }

    private fun doChange(change: ChangeSet) {
        mutableListOf<Action>().run {
            try {
                change.changes.forEach { action ->
                    action.executeIt()
                    add(action)
                }

                changelog.writeChangeToUser(change)
                LOG.info("Migration ${change.id}:${change.author} Successful executed: $size actions.")
            } catch (e: Exception) {
                LOG.error("Error occurred while migrating: ${e.message} ", e)
                LOG.error("Rolling back changes")
                rollback()
                throw e
            }
        }
    }

    private fun MutableList<Action>.rollback() {
        reverse()
        forEach {
            it.undoIt()
        }
    }
}