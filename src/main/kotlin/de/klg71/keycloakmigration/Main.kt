package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import de.klg71.keycloakmigration.changeControl.KeycloakMigration
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.slf4j.LoggerFactory

val LOG = LoggerFactory.getLogger("de.klg71.keycloakmigration")!!
val KOIN_LOGGER = LoggerFactory.getLogger("de.klg71.keycloakmigration.koinlogger")!!
const val defaultChangeLogFile = "keycloak-changelog.yml"
const val defaultAdminUser = "admin"
const val defaultAdminPassword = "admin"

class Args(parser: ArgParser) {
    val adminUser by parser.storing(names = *arrayOf("-u", "--user"),
            help = "Username for the migration user, defaulting to $defaultAdminUser.")
            .default(defaultAdminUser)

    val adminPassword by parser.storing(names = *arrayOf("-p", "--password"),
            help = "Password for the migration user ,defaulting to $defaultAdminPassword.")
            .default(defaultAdminPassword)

    val migrationFile by parser.positionalList(help = "File to migrate, default to $defaultChangeLogFile",
            sizeRange = 0..1)
}

fun main(args: Array<String>) {
    ArgParser(args).parseInto(::Args).let {
        migrate(it)
    }
}

fun migrate(args: Args) {
    args.run {
        startKoin(listOf(myModule(adminUser, adminPassword)), logger = KoinLogger(KOIN_LOGGER))
        if (migrationFile.isNotEmpty()) {
            KeycloakMigration(migrationFile.first())
        } else {
            LOG.info("Defaulting to $defaultChangeLogFile")
            KeycloakMigration(defaultChangeLogFile)
        }
        stopKoin()
    }
}
