package de.klg71.keycloakmigration.testmigration

import de.klg71.keycloakmigration.DEFAULT_ADMIN_PASSWORD
import de.klg71.keycloakmigration.DEFAULT_ADMIN_USER
import de.klg71.keycloakmigration.DEFAULT_CLIENTID
import de.klg71.keycloakmigration.DEFAULT_FAIL_ON_UNDEFINED_VARIABLES
import de.klg71.keycloakmigration.DEFAULT_KEYCLOAK_SERVER
import de.klg71.keycloakmigration.DEFAULT_REALM
import de.klg71.keycloakmigration.DEFAULT_WAIT_FOR_KEYCLOAK
import de.klg71.keycloakmigration.DEFAULT_WAIT_FOR_KEYCLOAK_TIMEOUT
import de.klg71.keycloakmigration.DEFAULT_DISABLE_WARN_ON_UNDEFINED_VARIABLES
import de.klg71.keycloakmigration.MigrationArgs
import de.klg71.keycloakmigration.migrate
import org.apache.logging.log4j.core.config.Configurator
import java.nio.file.Paths

/**
 * executes the changesets from test/resources
 *
 * INFO: This file must has its run configuration working dir set to src/test/resources
 */
object TestMigrationArgs : MigrationArgs {
    override fun adminUser() = DEFAULT_ADMIN_USER
    override fun adminPassword() = DEFAULT_ADMIN_PASSWORD
    override fun baseUrl() = DEFAULT_KEYCLOAK_SERVER
    override fun migrationFile() = "src/test/resources/keycloak-changelog.yml"
    override fun realm() = DEFAULT_REALM
    override fun clientId() = DEFAULT_CLIENTID
    override fun correctHashes() = false
    override fun parameters(): Map<String, String> {
        return mapOf("IS_TEST_ENV" to "true")
    }
    override fun waitForKeycloak() = DEFAULT_WAIT_FOR_KEYCLOAK
    override fun waitForKeycloakTimeout() = DEFAULT_WAIT_FOR_KEYCLOAK_TIMEOUT.toLong()
    override fun failOnUndefinedVariables() = DEFAULT_FAIL_ON_UNDEFINED_VARIABLES
    override fun warnOnUndefinedVariables() = DEFAULT_DISABLE_WARN_ON_UNDEFINED_VARIABLES
}

fun main() {
    Configurator.initialize(null, Paths.get("").toAbsolutePath().toString()+"/src/test/resources/log4j2.yml");
    migrate(TestMigrationArgs)
}
