package de.klg71.keycloakmigration.testmigration

import de.klg71.keycloakmigration.migrate
import org.apache.logging.log4j.core.config.Configurator
import java.nio.file.Paths

fun main() {
    Configurator.initialize(null, Paths.get("").toAbsolutePath().toString() + "/src/test/resources/log4j2.yml")
    migrate(TestMigrationArgs)
}
