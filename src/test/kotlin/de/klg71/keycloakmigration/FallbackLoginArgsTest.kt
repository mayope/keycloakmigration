package de.klg71.keycloakmigration

import com.xenomachina.argparser.ArgParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FallbackLoginArgsTest {

    private fun parse(vararg args: String) = ArgParser(arrayOf(*args)).parseInto(::CommandLineMigrationArgs)

    @Test
    fun fallbackIsOffByDefault() {
        val args = parse()

        assertThat(args.fallbackToPasswordLogin()).isFalse()
        assertThat(args.fallbackClientId()).isEqualTo("admin-cli")
    }

    @Test
    fun fallbackOptionsAreParsed() {
        val args = parse("-m", "--fallback-to-password-login", "--fallback-client", "other-cli")

        assertThat(args.fallbackToPasswordLogin()).isTrue()
        assertThat(args.fallbackClientId()).isEqualTo("other-cli")
    }
}
