package de.klg71.keycloakmigration.changeControl.actions

import de.klg71.keycloakmigration.KeycloakClient
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.slf4j.LoggerFactory

/**
 * Abstract class describing an keycloak migration Action
 *
 */
abstract class Action : KoinComponent {

    companion object {
        val LOG = LoggerFactory.getLogger(Action::class.java)!!
    }

    @Suppress("unused")
    protected val client by inject<KeycloakClient>()

    private var executed = false

    fun executeIt() {
        if (isRequired()) {
            execute()
            executed = true
        } else {
            LOG.info("Skipping migration: ${name()}")
        }
    }

    fun undoIt() {
        if(executed){
            LOG.info("Undo migration: ${name()}")
            undo()
        }
    }

    /**
     * Will only be executed if isRequired returns true
     */
    protected abstract fun execute()

    /**
     * Will only be executed if execute() was executed without an Exception
     */
    protected abstract fun undo()

    /**
     * Returns true if this migration is necessary
     */
    protected abstract fun isRequired(): Boolean

    /**
     * Returns the name of the migration for logging purposes
     */
    protected abstract fun name(): String
}