package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody

fun main(args: Array<String>) = mainBody {
    migrate(ArgParser(args).parseInto(::CommandLineMigrationArgs))
}
