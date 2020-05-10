package de.klg71.keycloakmigration.testmigration

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import de.klg71.keycloakmigration.CommandLineMigrationArgs
import de.klg71.keycloakmigration.migrate

/**
 * executes the changesets from test/resources
 *
 * INFO: This file must has its run configuration working dir set to src/test/resources
 */
fun main(args: Array<String>) = mainBody {
    migrate(ArgParser(args).parseInto(::CommandLineMigrationArgs))
}
