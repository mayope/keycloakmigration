package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import de.klg71.keycloakmigration.changeControl.KeycloakMigration
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.logger.SLF4JLogger
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.time.temporal.ChronoUnit

const val DEFAULT_WAIT_FOR_KEYCLOAK_PAUSE_TIME = 1000L
private const val STATUS_UNAUTHORISED = 401
private const val LOGGING_MODULO = 10

internal fun main(args: Array<String>) = mainBody {
    val migrationArgs = ArgParser(args).parseInto(::CommandLineMigrationArgs)
    migrate(migrationArgs)
}

internal fun waitForKeycloak(baseUrl: String, timeout: Long) {
    val waitTill = Instant.now().plus(timeout, ChronoUnit.SECONDS)
    var logCounter = 1
    while (true) {
        if (isKeycloakReady(baseUrl, logCounter % LOGGING_MODULO == 0)) return
        if (timeout > 0 && Instant.now().isAfter(waitTill)) {
            throw KeycloakNotReadyException()
        }
        logCounter += 1
        println("Waiting for Keycloak to become ready")
        Thread.sleep(DEFAULT_WAIT_FOR_KEYCLOAK_PAUSE_TIME)
    }
}

@Suppress("SwallowedException")
private fun isKeycloakReady(baseUrl: String, logError: Boolean): Boolean {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("$baseUrl/admin/realms"))
        .GET()
        .build()

    try {
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        return response.statusCode() == STATUS_UNAUTHORISED
    } catch (e: IOException) {
        if (logError) {
            println("Error: ${e.message}")
        }
    } catch (e: ConnectException) {
        if (logError) {
            println("Error: ${e.message}")
        }
    } catch (e: SocketException) {
        if (logError) {
            println("Error: ${e.message}")
        }
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
                logger(SLF4JLogger())
                modules(
                    myModule(
                        adminUser(), adminPassword(), adminTotp(),
                        adminUseOauth(), adminUseOauthLocalPort(),
                        baseUrl(), realm(), clientId(), parameters(),
                        failOnUndefinedVariables(), warnOnUndefinedVariables()
                    )
                )
                KeycloakMigration(
                    migrationFile(), realm(), correctHashes(), disableSetUnmanagedAttributesToAdminEdit()
                ).execute()
            }
        } finally {
            stopKoin()
        }
    }
}
