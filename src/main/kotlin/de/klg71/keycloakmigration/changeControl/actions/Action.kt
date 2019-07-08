package de.klg71.keycloakmigration.changeControl.actions

import de.klg71.keycloakmigration.rest.KeycloakClient
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.slf4j.LoggerFactory

/**
 * Abstract class describing an Keycloak migration Action
 *
 */
abstract class Action : KoinComponent {

    companion object {
        val LOG = LoggerFactory.getLogger(Action::class.java)!!
    }

    lateinit var path:String

    @Suppress("unused")
    protected val client by inject<KeycloakClient>()

    private var executed = false

    fun executeIt() {
        LOG.info("Executing migration: ${name()}")
        execute()
        executed = true
    }

    fun undoIt() {
        if (executed) {
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
     * Hash of the migration to check if its already executed
     */
    abstract fun hash(): String

    /**
     * Returns the name of the migration for logging purposes
     */
    abstract fun name(): String
}