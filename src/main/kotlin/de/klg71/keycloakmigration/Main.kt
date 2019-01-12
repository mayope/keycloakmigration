package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import de.klg71.keycloakmigration.changeControl.KeycloakMigrationExecute
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.slf4j.LoggerFactory

val KOIN_LOGGER = LoggerFactory.getLogger("de.klg71.keycloakmigration.koinlogger")!!
const val defaultChangeLogFile = "keycloak-changelog.yml"
const val defaultAdminUser = "admin"
const val defaultAdminPassword = "admin"
const val defaultKeycloakServer = "http://localhost:18080/auth"

interface MigrationArgs {
    fun adminUser(): String
    fun adminPassword(): String
    fun baseUrl(): String
    fun migrationFile(): String
}

class CommandLineMigrationArgs(parser: ArgParser) : MigrationArgs {
    private val adminUser by parser.storing(names = *arrayOf("-u", "--user"),
            help = "Username for the migration user, defaulting to $defaultAdminUser.")
            .default(defaultAdminUser)

    private val adminPassword by parser.storing(names = *arrayOf("-p", "--password"),
            help = "Password for the migration user ,defaulting to $defaultAdminPassword.")
            .default(defaultAdminPassword)

    private val baseUrl by parser.storing(names = *arrayOf("-b", "--baseurl"),
            help = "Base url of keycloak server ,defaulting to $defaultKeycloakServer.")
            .default(defaultKeycloakServer)

    private val migrationFile by parser.positionalList(help = "File to migrate, default to $defaultChangeLogFile",
            sizeRange = 0..1)

    override fun adminUser() = adminUser

    override fun adminPassword() = adminPassword

    override fun migrationFile() = migrationFile.firstOrNull() ?: defaultChangeLogFile

    override fun baseUrl() = baseUrl

}

fun main(args: Array<String>) {
    migrate(ArgParser(args).parseInto(::CommandLineMigrationArgs))
}

fun migrate(commandLineMigrationArgs: MigrationArgs) {
    commandLineMigrationArgs.run {
        startKoin(listOf(myModule(adminUser(), adminPassword(), baseUrl())), logger = KoinLogger(KOIN_LOGGER))
        KeycloakMigrationExecute(migrationFile())
        stopKoin()
    }
}
