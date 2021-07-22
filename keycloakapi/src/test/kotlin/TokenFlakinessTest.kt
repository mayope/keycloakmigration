package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.RealmUpdateBuilder
import de.klg71.keycloakmigration.keycloakapi.model.User
import org.apache.logging.log4j.core.config.Configurator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.lang.System.nanoTime
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.test.fail

class TokenFlakinessTest {

    private val LOG = LoggerFactory.getLogger(TokenFlakinessTest::class.java)

    @Test
    @Ignore
    fun testFlakiness() {

        Configurator.initialize(null, Paths.get("").toAbsolutePath().toString() + "/src/test/resources/log4j2.yml")
        val masterTokenLifespan = 1

        val baseUrl = "http://localhost:18080/auth"
        val adminUser = "admin"
        val realmName = "master"
        val clientId = "admin-cli"
        val adminPassword = "admin"
        val tokenHolder =
            TokenHolder(initKeycloakLoginClient(baseUrl), adminUser, adminPassword, realmName, clientId, "", 0)
        val configureClient = initKeycloakClientWithTokenHolder(baseUrl, tokenHolder = tokenHolder)
        val realm = configureClient.realmById(realmName)
        val updatedRealm = RealmUpdateBuilder(realm).run {
            this.accessTokenLifespan = masterTokenLifespan
            this.accessTokenLifespanForImplicitFlow = masterTokenLifespan
            build()
        }
        configureClient.updateRealm(realmName, updatedRealm)

        repeat(10000) {
            val preTokenTestDurationBeforeStartNs = 4
            val client =
                initKeycloakClient(baseUrl, adminUser, adminPassword, realmName, clientId, null, "", 20)
            val testCallStartTimeMs =
                currentTimeMillis() + TimeUnit.SECONDS.toMillis(
                    tokenHolder.token().expiresIn
                ) - preTokenTestDurationBeforeStartNs
            while (currentTimeMillis() < testCallStartTimeMs) {
            }
            val time = nanoTime()
            while (nanoTime() < (time + 990))
                if (!testCall(client)) {
                    fail("keycloak call has failed!")
                }
        }
    }

    private fun testCall(client: KeycloakClient): Boolean {
        try {
            assertThat(client.searchByUsername("admin", "master").map(User::username))
                .containsExactlyInAnyOrder("admin")
        } catch (ex: Throwable) {
            LOG.error("testCall failed", ex)
            return false
        }
        LOG.info("testCall done")
        return true
    }
}
