package de.klg71.keycloakmigration.testmigration.correcthashes

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import de.klg71.keycloakmigration.CommandLineMigrationArgs
import de.klg71.keycloakmigration.migrate

/**
 * executes the changesets from test/resources with the correct-hashes switch on
 * so you can manually corrupt the hashes and try to fix it with this config
 *
 * INFO: This file must has its run configuration set to test/resources
 */
fun main() = mainBody {
    migrate(
            ArgParser(arrayOf("--correct-hashes")).parseInto(::CommandLineMigrationArgs))
}
