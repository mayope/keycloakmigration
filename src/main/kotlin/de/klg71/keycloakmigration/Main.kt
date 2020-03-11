package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import de.klg71.keycloakmigration.changeControl.KeycloakMigration
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.slf4j.LoggerFactory

val KOIN_LOGGER = LoggerFactory.getLogger("de.klg71.keycloakmigration.koinlogger")!!
const val defaultChangeLogFile = "keycloak-changelog.yml"
const val defaultAdminUser = "admin"
const val defaultAdminPassword = "admin"
const val defaultKeycloakServer = "http://localhost:18080/auth"
const val defaultRealm = "master"
const val defaultClientId = "admin-cli"
const val defaultCorrectHashes = false

interface MigrationArgs {
    fun adminUser(): String
    fun adminPassword(): String
    fun baseUrl(): String
    fun migrationFile(): String
    fun realm(): String
    fun clientId(): String
    fun correctHashes(): Boolean
    fun parameters(): Map<String, String>
}

internal class CommandLineMigrationArgs(parser: ArgParser) : MigrationArgs {
    private val adminUser by parser.storing(names = *arrayOf("-u", "--user"),
            help = "Username for the migration user, defaulting to $defaultAdminUser.")
            .default(defaultAdminUser)

    private val adminPassword by parser.storing(names = *arrayOf("-p", "--password"),
            help = "Password for the migration user, defaulting to $defaultAdminPassword.")
            .default(defaultAdminPassword)

    private val baseUrl by parser.storing(names = *arrayOf("-b", "--baseurl"),
            help = "Base url of keycloak server, defaulting to $defaultKeycloakServer.")
            .default(defaultKeycloakServer)

    private val migrationFile by parser.positionalList(help = "File to migrate, defaulting to $defaultChangeLogFile",
            sizeRange = 0..1)

    private val realm by parser.storing(names = *arrayOf("-r", "--realm"),
            help = "Realm to use for migration, defaulting to $defaultRealm")
            .default(defaultRealm)

    private val clientId by parser.storing(names = *arrayOf("-c", "--client"),
            help = "Client to use for migration, defaulting to $defaultRealm")
            .default(defaultClientId)

    private val correctHashes by parser.flagging(names = *arrayOf("--correct-hashes"),
            help = "Correct hashes to most recent version, defaulting to $defaultCorrectHashes \r\n" +
                    "Just choose this option if you didn't change anything in the changelog since the last migration! \n" +
                    "This will replace all old hashes with the new hash version and can be omitted next time the migration is run.\n" +
                    "See README.md for further explanation!").default(
            defaultCorrectHashes)

    private val parameters by parser.adding(names = *arrayOf("-k", "--parameter"),
            help = "Parameters to substitute in changelog, syntax is: -k param1=value1 will replace \${param1} with value1 in changelog").default(
            emptyList<String>())

    override fun adminUser() = adminUser

    override fun adminPassword() = adminPassword

    override fun migrationFile() = migrationFile.firstOrNull() ?: defaultChangeLogFile

    override fun baseUrl() = baseUrl

    override fun realm() = realm

    override fun clientId() = clientId

    override fun correctHashes() = correctHashes

    override fun parameters(): Map<String, String> = parameters.map {
        if (!it.contains("=")) {
            throw RuntimeException("Invalid parameter detected: $it, syntax for parameter is param1=value1!")
        }
        it.split("=").run {
            first() to get(1)
        }
    }.run {
        toMap()
    }

}

fun main(args: Array<String>) = mainBody {
    migrate(ArgParser(args).parseInto(::CommandLineMigrationArgs))
}

fun migrate(migrationArgs: MigrationArgs) {
    migrationArgs.run {
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
