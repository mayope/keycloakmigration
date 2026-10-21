package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CommandLineMigrationArgsTest {

    private fun parse(vararg args: String) = ArgParser(arrayOf(*args)).parseInto(::CommandLineMigrationArgs)

    @Test
    fun totpIsEmptyByDefault() {
        assertThat(parse().adminTotp()).isEmpty()
    }

    @Test
    fun parameterValueMayContainEquals() {
        assertThat(parse("-k", "query=a=b").parameters()).containsEntry("query", "a=b")
    }

    @Test
    fun oauthLocalPortTakesAValue() {
        val args = parse("-P", "9000", "changelog.yml")

        assertThat(args.adminUseOauthLocalPort()).isEqualTo(9000)
        assertThat(args.migrationFile()).isEqualTo("changelog.yml")
    }

    @Test
    fun oauthLocalPortHasDefault() {
        assertThat(parse().adminUseOauthLocalPort()).isEqualTo(DEFAULT_ADMIN_USE_OAUTH_LOCAL_PORT)
    }
}
