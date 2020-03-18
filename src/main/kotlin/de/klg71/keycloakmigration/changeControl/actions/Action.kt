package de.klg71.keycloakmigration.changeControl.actions

import de.klg71.keycloakmigration.rest.KeycloakClient
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory

/**
 * Abstract class describing an Keycloak migration Action
 *
 */
abstract class Action(var realm: String? = null) : KoinComponent {

    companion object {
        val LOG = LoggerFactory.getLogger(Action::class.java)!!
    }

    lateinit var path: String
    lateinit var yamlNodeValue: String

    @Suppress("unused")
    protected val client by inject<KeycloakClient>()

    private var executed = false

    /**
     * Executes the Action
     */
    fun executeIt() {
        LOG.info("Executing migration: ${name()}")
        execute()
        executed = true
    }

    /**
     * Undos the Action
     */
    fun undoIt() {
        if (executed) {
            LOG.info("Undo migration: ${name()}")
            undo()
        }
    }

    /**
     * Will only be executed if isRequired returns true
     *
     * @implNote This method contains the change procedure for the migration
     */
    protected abstract fun execute()

    /**
     * Will only be executed if execute() was executed without an Exception
     *
     * @implNote This method contains the change-undo procedure for the migration
     */
    protected abstract fun undo()

    /**
     * Returns the name of the migration for logging purposes
     */
    abstract fun name(): String

    protected fun realm(): String = realm
            ?: throw de.klg71.keycloakmigration.changeControl.ParseException(
                    "Realm is null for ${name()}, either provide it in the change or the changeset!")
}
