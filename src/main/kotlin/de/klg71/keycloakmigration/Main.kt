package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.changeControl.KeycloakMigration
import org.koin.log.Logger
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.slf4j.LoggerFactory

val LOG = LoggerFactory.getLogger("de.klg71.keycloakmigration")!!
val KOIN_LOGGER = LoggerFactory.getLogger("de.klg71.keycloakmigration.koinlogger")!!
const val defaultChangeLogFile = "keycloak-changelog.yml"

class KoinLogger(private val log: org.slf4j.Logger) : Logger {
    override fun debug(msg: String) {
        log.debug(msg)
    }

    override fun err(msg: String) {
        log.error(msg)
    }

    override fun info(msg: String) {
        log.info(msg)
    }
}

fun main(args: Array<String>) {

    startKoin(listOf(myModule), logger = KoinLogger(KOIN_LOGGER))
    if (args.isNotEmpty()) {
        KeycloakMigration(args[0])
    } else {
        LOG.info("Defaulting to $defaultChangeLogFile")
        KeycloakMigration(defaultChangeLogFile)
    }

    stopKoin()
}
