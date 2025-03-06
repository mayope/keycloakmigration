package de.klg71.keycloakmigration.changeControl

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.realm.UpdateRealmAction
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Execute the keycloakmigration
 */
internal class KeycloakMigration(private val migrationFile: String, private val realm: String,
    private val correctHashes: Boolean,
    private val disableSetUnmanagedAttributesToAdminEdit: Boolean) : KoinComponent {
    private val migrationUserId by inject<UUID>(named("migrationUserId"))
    private val changeFileReader = ChangeFileReader()
    private val changelog = MigrationChangelog(migrationUserId, realm)

    companion object {
        val LOG = LoggerFactory.getLogger(KeycloakMigration::class.java)!!
    }

    @Suppress("TooGenericExceptionCaught")
    internal fun execute() {
        try {
            if (!disableSetUnmanagedAttributesToAdminEdit) {
                UpdateRealmAction(realm, unmanagedAttributePolicy = "ADMIN_EDIT").executeIt()
            }
            changeFileReader.changes(migrationFile).let {
                changelog.changesTodo(it, correctHashes)
            }.forEach { change ->
                LOG.info("Executing change: ${change.id}:${change.author}")
                doChange(change)
            }
        } catch (e: Throwable) {
            LOG.error("Migrations were unsuccessful:", e)
            throw e
        }

    }

    @Suppress("TooGenericExceptionCaught")
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
                LOG.error("Error occurred while migrating: ${change.id} ", e)
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
