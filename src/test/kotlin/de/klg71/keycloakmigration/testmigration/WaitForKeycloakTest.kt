package de.klg71.keycloakmigration.testmigration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.matching.UrlPattern
import de.klg71.keycloakmigration.DEFAULT_ADMIN_PASSWORD
import de.klg71.keycloakmigration.DEFAULT_ADMIN_USER
import de.klg71.keycloakmigration.DEFAULT_ADMIN_USE_OAUTH
import de.klg71.keycloakmigration.DEFAULT_ADMIN_USE_OAUTH_LOCAL_PORT
import de.klg71.keycloakmigration.DEFAULT_CLIENTID
import de.klg71.keycloakmigration.DEFAULT_DISABLE_WARN_ON_UNDEFINED_VARIABLES
import de.klg71.keycloakmigration.DEFAULT_FAIL_ON_UNDEFINED_VARIABLES
import de.klg71.keycloakmigration.DEFAULT_REALM
import de.klg71.keycloakmigration.KeycloakNotReadyException
import de.klg71.keycloakmigration.MigrationArgs
import de.klg71.keycloakmigration.migrate
import org.apache.logging.log4j.core.config.Configurator
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.error.InstanceCreationException
import java.nio.file.Paths


class WaitForKeycloakTest {

    object TestMigrationArgs : MigrationArgs {
        override fun adminUser() = DEFAULT_ADMIN_USER
        override fun adminPassword() = DEFAULT_ADMIN_PASSWORD
        override fun adminUseOauth() = DEFAULT_ADMIN_USE_OAUTH
        override fun adminUseOauthLocalPort() = DEFAULT_ADMIN_USE_OAUTH_LOCAL_PORT
        override fun baseUrl() = "http://localhost:8888/auth" // wiremock server
        override fun migrationFile() = "src/test/resources/keycloak-changelog.yml"
        override fun realm() = DEFAULT_REALM
        override fun clientId() = DEFAULT_CLIENTID
        override fun correctHashes() = false
        override fun parameters(): Map<String, String> {
            return mapOf("IS_TEST_ENV" to "true")
        }

        override fun waitForKeycloak() = true
        override fun waitForKeycloakTimeout() = 20L // seconds
        override fun failOnUndefinedVariables() = DEFAULT_FAIL_ON_UNDEFINED_VARIABLES
        override fun warnOnUndefinedVariables() = DEFAULT_DISABLE_WARN_ON_UNDEFINED_VARIABLES
        override fun adminTotp() = ""
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
            wireMockServer.stubFor(
                get(UrlPattern.ANY).willReturn(ok().withBody("ok"))
            )
        }.start()

        Configurator.initialize(null, Paths.get("").toAbsolutePath().toString() + "/src/test/resources/log4j2.yml");
        assertThatThrownBy { migrate(TestMigrationArgs) }.isInstanceOf(
            InstanceCreationException::class.java
        ) // since wiremock will return nothing
    }

}
