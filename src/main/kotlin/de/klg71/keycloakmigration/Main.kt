package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import de.klg71.keycloakmigration.changeControl.KeycloakMigration
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.net.SocketException
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit

val KOIN_LOGGER = LoggerFactory.getLogger("de.klg71.keycloakmigration.koinlogger")!!
const val DEFAULT_WAIT_FOR_KEYCLOAK_PAUSE_TIME = 1000L

fun main(args: Array<String>) = mainBody {
    val migrationArgs = ArgParser(args).parseInto(::CommandLineMigrationArgs)
    migrate(migrationArgs)
}

fun waitForKeycloak(baseUrl: String, timeout: Long) {
    val waitTill = Instant.now().plus(timeout, ChronoUnit.SECONDS)
    while (true) {
        if (isKeycloakReady(baseUrl)) return
        if (timeout > 0 && waitTill.isAfter(Instant.now())) {
            throw KeycloakNotReadyException()
        }
        println("Waiting for Keycloak to become ready")
        Thread.sleep(DEFAULT_WAIT_FOR_KEYCLOAK_PAUSE_TIME)
    }
}

private fun isKeycloakReady(baseUrl: String): Boolean {
    try {
        if (URL(baseUrl).readBytes().isNotEmpty())
            return true
    } catch (e: ConnectException) {
        // nothing to do
    } catch (e: SocketException) {
        // nothing to do
    }
    return false
}

fun migrate(migrationArgs: MigrationArgs) {
    migrationArgs.run {
        if (waitForKeycloak()) {
            waitForKeycloak(migrationArgs.baseUrl(), migrationArgs.waitForKeycloakTimeout())
        }
        try {
            startKoin {
                logger(KoinLogger(KOIN_LOGGER))
                modules(myModule(adminUser(), adminPassword(), baseUrl(), realm(), clientId(), parameters()))
                KeycloakMigration(migrationFile(), realm(), correctHashes(), failOnUndefinedVariables(),
                        warnOnUndefinedVariables()).execute()
            }
        } finally {
            stopKoin()
        }
    }
}
