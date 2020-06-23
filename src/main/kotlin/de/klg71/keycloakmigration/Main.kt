package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.DefaultHelpFormatter
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
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
const val DEFAULT_CHANGELOGFILE = "keycloak-changelog.yml"
const val DEFAULT_ADMIN_USER = "admin"
const val DEFAULT_ADMIN_PASSWORD = "admin"
const val DEFAULT_KEYCLOAK_SERVER = "http://localhost:18080/auth"
const val DEFAULT_REALM = "master"
const val DEFAULT_CLIENTID = "admin-cli"
const val DEFAULT_CORRECT_HASHES = false
const val DEFAULT_WAIT_FOR_KEYCLOAK = false
const val DEFAULT_WAIT_FOR_KEYCLOAK_TIMEOUT = "0"

const val DEFAULT_WAIT_FOR_KEYCLOAK_PAUSE_TIME = 1000L

interface MigrationArgs {
    fun adminUser(): String
    fun adminPassword(): String
    fun baseUrl(): String
    fun migrationFile(): String
    fun realm(): String
    fun clientId(): String
    fun correctHashes(): Boolean
    fun parameters(): Map<String, String>
    fun waitForKeycloak(): Boolean
    fun waitForKeycloakTimeout(): Long
}

@Suppress("SpreadOperator")
internal class CommandLineMigrationArgs(parser: ArgParser) : MigrationArgs {
    private val adminUser by parser.storing(names = *arrayOf("-u", "--user"),
            help = "Username for the migration user, defaulting to $DEFAULT_ADMIN_USER.")
            .default(DEFAULT_ADMIN_USER)

    private val adminPassword by parser.storing(names = *arrayOf("-p", "--password"),
            help = "Password for the migration user, defaulting to $DEFAULT_ADMIN_PASSWORD.")
            .default(DEFAULT_ADMIN_PASSWORD)

    private val baseUrl by parser.storing(names = *arrayOf("-b", "--baseurl"),
            help = "Base url of keycloak server, defaulting to $DEFAULT_KEYCLOAK_SERVER.")
            .default(DEFAULT_KEYCLOAK_SERVER)

    private val migrationFile by parser.positionalList(help = "File to migrate, defaulting to $DEFAULT_CHANGELOGFILE",
            sizeRange = 0..1)

    private val realm by parser.storing(names = *arrayOf("-r", "--realm"),
            help = "Realm to use for migration, defaulting to $DEFAULT_REALM")
            .default(DEFAULT_REALM)

    private val clientId by parser.storing(names = *arrayOf("-c", "--client"),
            help = "Client to use for migration, defaulting to $DEFAULT_REALM")
            .default(DEFAULT_CLIENTID)

    private val correctHashes by parser.flagging(names = *arrayOf("--correct-hashes"),
            help = "Correct hashes to most recent version, defaulting to $DEFAULT_CORRECT_HASHES \r\n" +
                    "Just choose this option if you didn't change anything" +
                    " in the changelog since the last migration! \n" +
                    "This will replace all old hashes with the new hash" +
                    " version and can be omitted next time the migration is run.\n" +
                    "See README.md for further explanation!").default(
            DEFAULT_CORRECT_HASHES)

    private val parameters by parser.adding(names = *arrayOf("-k", "--parameter"),
            help = "Parameters to substitute in changelog," +
                    " syntax is: -k param1=value1 will replace \${param1} with value1 in changelog")
            .default(emptyList<String>())

    private val waitForKeycloak by parser.flagging(names = *arrayOf("--wait-for-keycloak"),
            help = "Wait for Keycloak to become ready.").default(DEFAULT_WAIT_FOR_KEYCLOAK)

    private val waitForKeycloakTimout by parser.storing(names = *arrayOf("--wait-for-keycloak-timeout"),
            help = "Wait for Keycloak to become ready timeout in seconds (defaulting to 0=infinit).")
            .default(DEFAULT_WAIT_FOR_KEYCLOAK_TIMEOUT)

    override fun adminUser() = adminUser

    override fun adminPassword() = adminPassword

    override fun migrationFile() = migrationFile.firstOrNull() ?: DEFAULT_CHANGELOGFILE

    override fun baseUrl() = baseUrl

    override fun realm() = realm

    override fun clientId() = clientId

    override fun correctHashes() = correctHashes

    override fun parameters(): Map<String, String> = parameters.map {
        if (!it.contains("=")) {
            throw InvalidArgumentException("Invalid parameter detected: $it, syntax for parameter is param1=value1!")
        }
        it.split("=").run {
            first() to get(1)
        }
    }.run {
        toMap()
    }

    override fun waitForKeycloak() = waitForKeycloak

    override fun waitForKeycloakTimeout() = waitForKeycloakTimout.toLong()
}

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
                KeycloakMigration(migrationFile(), realm(), correctHashes()).execute()
            }
        } finally {
            stopKoin()
        }
    }
}
