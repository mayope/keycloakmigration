package de.klg71.keycloakmigration

import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE


/**
 * Wrapper for slf4j Logger into koin compatible logger
 */
class KoinLogger(private val log: org.slf4j.Logger) : Logger() {
    override fun log(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> log.debug(msg)
            Level.ERROR -> log.error(msg)
            Level.INFO -> log.info(msg)
            Level.NONE -> TODO()
        }
    }

}
