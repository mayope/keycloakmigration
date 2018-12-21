package de.klg71.keycloakmigration

import org.koin.log.Logger

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