package de.klg71.keycloakmigration.changeControl

import de.klg71.keycloakmigration.KoinLogger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.slf4j.LoggerFactory

class StringEnvSubstitutorTest {

    private val LOG = LoggerFactory.getLogger(StringEnvSubstitutorTest::class.java)!!

    private fun startKoin(parameters: Map<String, String>) {
        startKoin {
            logger(KoinLogger(LOG))
            modules(module {
                single(named("parameters")) {
                    parameters
                }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testSubstituteParameters_shouldFailOnMissing() {
        startKoin(emptyMap())
        val substitutor = StringEnvSubstitutor(failOnUndefinedVariables = true)

        assertThatThrownBy {
            substitutor.substituteParameters("\${NOT_SUBSTITUTED}")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Cannot resolve variable 'NOT_SUBSTITUTED' (enableSubstitutionInVariables=false).")
    }

    @Test
    fun testSubstituteParameters_shouldSucceedIfPresent() {

        val value = "test"
        startKoin(mapOf("SUBSTITUTED" to value))
        val substitutor = StringEnvSubstitutor(failOnUndefinedVariables = true)
        assertThat(substitutor.substituteParameters("\${SUBSTITUTED}")).isEqualTo(value)
    }

    @Test
    fun testSubstituteParameters_shouldIgnoreOnMissing() {
        startKoin(emptyMap())
        val substitutor = StringEnvSubstitutor(failOnUndefinedVariables = false)

        val value = "\${NOT_SUBSTITUTED}"
        assertThat(substitutor.substituteParameters(value)).isEqualTo(value)
    }

}
