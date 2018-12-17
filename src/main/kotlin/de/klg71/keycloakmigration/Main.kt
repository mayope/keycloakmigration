package de.klg71.keycloakmigration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.ChangeFile
import de.klg71.keycloakmigration.model.AccessToken
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.slf4j.LoggerFactory


fun main(args: Array<String>) {

    startKoin(listOf(myModule))

    KeycloakMigration()

    stopKoin()
}

class KeycloakMigration : KoinComponent {
    private val yamlObjectMapper by inject<ObjectMapper>(name = "yamlObjectMapper")

    companion object {
        val LOG = LoggerFactory.getLogger(KeycloakMigration::class.java)!!
    }

    init {
        val loader = Thread.currentThread().contextClassLoader
        val set = yamlObjectMapper.readValue<ChangeFile>(loader.getResourceAsStream("testMigration/changesets/initial.yml"))

        val executedChanges = mutableListOf<Action>()
        try {
            set.keycloakChangeSet.changes.forEach {
                    it.executeIt()
                    executedChanges.add(it)
            }
        } catch (e: Exception) {
            LOG.error("Error occurred while migrating: ", e)
            LOG.error("Rolling back changes", e)
            executedChanges.forEach {
                it.undoIt()
            }
        }

    }
}
