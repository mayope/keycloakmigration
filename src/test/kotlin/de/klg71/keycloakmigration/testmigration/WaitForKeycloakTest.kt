package de.klg71.keycloakmigration.testmigration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import de.klg71.keycloakmigration.*
import org.apache.logging.log4j.core.config.Configurator
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.io.FileNotFoundException
import java.nio.file.Paths


class WaitForKeycloakTest {

    /**
     * executes the changesets from test/resources
     *
     * INFO: This file must has its run configuration working dir set to src/test/resources
     */
    object TestMigrationArgs : MigrationArgs {
        override fun adminUser() = DEFAULT_ADMIN_USER
        override fun adminPassword() = DEFAULT_ADMIN_PASSWORD
        override fun baseUrl() = "http://localhost:8888/auth"
        override fun migrationFile() = "src/test/resources/keycloak-changelog.yml"
        override fun realm() = DEFAULT_REALM
        override fun clientId() = DEFAULT_CLIENTID
        override fun correctHashes() = false
        override fun parameters(): Map<String, String> {
            return mapOf("IS_TEST_ENV" to "true")
        }

        override fun waitForKeycloak() = true
        override fun waitForKeycloakTimeout() = 10L
        override fun failOnUndefinedVariables() = DEFAULT_FAIL_ON_UNDEFINED_VARIABLES
        override fun warnOnUndefinedVariables() = DEFAULT_DISABLE_WARN_ON_UNDEFINED_VARIABLES
    }

    @Test(timeout = 12 * 60 * 1000)
    fun `should throw KeycloakNotReadyException when timeout is reached`() {
        Configurator.initialize(null, Paths.get("").toAbsolutePath().toString() + "/src/test/resources/log4j2.yml");
        assertThatThrownBy { migrate(TestMigrationArgs) }.isInstanceOf(KeycloakNotReadyException::class.java)
    }

    @Test
    internal fun `should wait for keycloak to start`() {
        Thread {
            val wireMockServer = WireMockServer(options().port(8888))
            Thread.sleep(5000)
            wireMockServer.start()
        }.start()

        Configurator.initialize(null, Paths.get("").toAbsolutePath().toString() + "/src/test/resources/log4j2.yml");
        assertThatThrownBy { migrate(TestMigrationArgs) }.isInstanceOf(FileNotFoundException::class.java) // since wiremock will return nothing
    }

}