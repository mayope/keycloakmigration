package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default

const val DEFAULT_CHANGELOGFILE = "keycloak-changelog.yml"
const val DEFAULT_ADMIN_USER = "admin"
const val DEFAULT_ADMIN_PASSWORD = "admin"
const val DEFAULT_KEYCLOAK_SERVER = "http://localhost:8080/auth"
const val DEFAULT_REALM = "master"
const val DEFAULT_CLIENTID = "admin-cli"
const val DEFAULT_CORRECT_HASHES = false
const val DEFAULT_WAIT_FOR_KEYCLOAK = false
const val DEFAULT_WAIT_FOR_KEYCLOAK_TIMEOUT = "0"
const val DEFAULT_FAIL_ON_UNDEFINED_VARIABLES = false
const val DEFAULT_DISABLE_WARN_ON_UNDEFINED_VARIABLES = false

@Suppress("SpreadOperator", "TooManyFunctions")
internal class CommandLineMigrationArgs(parser: ArgParser) :
    MigrationArgs {
    private val adminUser by parser.storing(
        names = arrayOf("-u", "--user"),
        help = "Username for the migration user, defaulting to $DEFAULT_ADMIN_USER."
    )
        .default(DEFAULT_ADMIN_USER)

    private val adminPassword by parser.storing(
        names = arrayOf("-p", "--password"),
        help = "Password for the migration user, defaulting to $DEFAULT_ADMIN_PASSWORD."
    )
        .default(DEFAULT_ADMIN_PASSWORD)

    private val adminTotp by parser.storing(
        names = arrayOf("-t", "--totp"),
        help = "Time based one time password for the migration user, empty per default"
    )
        .default(DEFAULT_ADMIN_PASSWORD)

    private val baseUrl by parser.storing(
        names = arrayOf("-b", "--baseurl"),
        help = "Base url of keycloak server, defaulting to $DEFAULT_KEYCLOAK_SERVER."
    )
        .default(DEFAULT_KEYCLOAK_SERVER)

    private val migrationFile by parser.positionalList(
        help = "File to migrate, defaulting to $DEFAULT_CHANGELOGFILE",
        sizeRange = 0..1
    )

    private val realm by parser.storing(
        names = arrayOf("-r", "--realm"),
        help = "Realm to use for migration, defaulting to $DEFAULT_REALM"
    )
        .default(DEFAULT_REALM)

    private val clientId by parser.storing(
        names = arrayOf("-c", "--client"),
        help = "Client to use for migration, defaulting to $DEFAULT_CLIENTID"
    )
        .default(DEFAULT_CLIENTID)

    private val correctHashes by parser.flagging(
        names = arrayOf("--correct-hashes"),
        help = "Correct hashes to most recent version, defaulting to $DEFAULT_CORRECT_HASHES \r\n" +
                "Just choose this option if you didn't change anything" +
                " in the changelog since the last migration! \n" +
                "This will replace all old hashes with the new hash" +
                " version and can be omitted next time the migration is run.\n" +
                "See README.md for further explanation!"
    ).default(
        DEFAULT_CORRECT_HASHES
    )

    private val parameters by parser.adding(
        names = arrayOf("-k", "--parameter"),
        help = "Parameters to substitute in changelog," +
                " syntax is: -k param1=value1 will replace \${param1} with value1 in changelog"
    )
        .default(emptyList<String>())

    private val waitForKeycloak by parser.flagging(
        names = arrayOf("--wait-for-keycloak"),
        help = "Wait for Keycloak to become ready, defaulting to $DEFAULT_WAIT_FOR_KEYCLOAK."
    ).default(
        DEFAULT_WAIT_FOR_KEYCLOAK
    )

    private val waitForKeycloakTimout by parser.storing(
        names = arrayOf("--wait-for-keycloak-timeout"),
        help = "Wait for Keycloak to become ready timeout in seconds (defaulting to 0=infinit)."
    )
        .default(DEFAULT_WAIT_FOR_KEYCLOAK_TIMEOUT)

    private val failOnUndefinedVariables by parser.flagging(
        names = arrayOf("--fail-on-undefined-variables"),
        help = "Fail if variables could not be replaced, defaulting to $DEFAULT_FAIL_ON_UNDEFINED_VARIABLES."
    )
        .default(
            DEFAULT_FAIL_ON_UNDEFINED_VARIABLES
        )

    private val disableWarnOnUndefinedVariables
            by parser.flagging(
                names = arrayOf("--disable-warn-on-undefined-variables"),
                help = "Disables warning if variables could not be replaced," +
                        " defaulting to $DEFAULT_DISABLE_WARN_ON_UNDEFINED_VARIABLES."
            )
                .default(DEFAULT_DISABLE_WARN_ON_UNDEFINED_VARIABLES)

    override fun adminUser() = adminUser

    override fun adminPassword() = adminPassword
    override fun adminTotp() = adminTotp

    override fun migrationFile() = migrationFile.firstOrNull() ?: DEFAULT_CHANGELOGFILE

    override fun baseUrl() = baseUrl

    override fun realm() = realm

    override fun clientId() = clientId

    override fun correctHashes() = correctHashes

    override fun parameters(): Map<String, String> = parameters.map {
        if (!it.contains("=")) {
            throw InvalidArgumentException(
                "Invalid parameter detected: $it, syntax for parameter is param1=value1!"
            )
        }
        it.split("=").run {
            first() to get(1)
        }
    }.run {
        toMap()
    }

    override fun waitForKeycloak() = waitForKeycloak

    override fun waitForKeycloakTimeout() = waitForKeycloakTimout.toLong()

    override fun failOnUndefinedVariables() = failOnUndefinedVariables

    override fun warnOnUndefinedVariables() = !disableWarnOnUndefinedVariables
}
