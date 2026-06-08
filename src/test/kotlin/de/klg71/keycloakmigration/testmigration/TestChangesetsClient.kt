package de.klg71.keycloakmigration.testmigration

import de.klg71.keycloakmigration.migrate
import org.apache.logging.log4j.core.config.Configurator
import java.nio.file.Paths

/**
 * Client needs to be created in admin ui according to TestMigrationArgsClient
 */
fun main() {
    Configurator.initialize(null, Paths.get("").toAbsolutePath().toString() + "/src/test/resources/log4j2.yml")
    migrate(TestMigrationArgsClient)
}
