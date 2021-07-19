package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.changeControl.MigrationChangelogTest
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.initKeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.RealmUpdateBuilder
import de.klg71.keycloakmigration.keycloakapi.model.User
import de.klg71.keycloakmigration.keycloakapi.realmById
import feign.slf4j.Slf4jLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.slf4j.LoggerFactory
import java.util.function.Supplier
import kotlin.test.fail

class TokenFlakinessTest {

    private val LOG = LoggerFactory.getLogger(MigrationChangelogTest::class.java)
    private val feignLOG = Slf4jLogger(MigrationChangelogTest::class.java)

    @Test
    fun testFlakiness() {
        val masterTokenLifespan = 2000L

        val baseUrl = "http://localhost:18080/auth"
        val adminUser = "admin"
        val realmName = "master"
        val clientId = "admin-cli"
        val adminPassword = "admin"
        val configureClient = initKeycloakClient(baseUrl, adminUser, adminPassword, realmName, clientId, feignLOG)
        val realm = configureClient.realmById(realmName)
        val updatedRealm = RealmUpdateBuilder(realm).run {
            this.accessTokenLifespan = masterTokenLifespan.toInt()/1000
            this.accessTokenLifespanForImplicitFlow = masterTokenLifespan.toInt()/1000
            build()
        }
        configureClient.updateRealm(realmName, updatedRealm)

        repeat(10000) {
            val preTokenTestDurationBeforeStartNs = 10L
            var tokenRefreshTimeNsSupplier: Supplier<Long>? = null
            val client = initKeycloakClient(baseUrl, adminUser, adminPassword, realmName, clientId, feignLOG, tokenRefreshTimeNs = {
                tokenRefreshTimeNsSupplier = it
            })
            val testCallStartTimeNs = tokenRefreshTimeNsSupplier!!.get() - preTokenTestDurationBeforeStartNs
            while (System.nanoTime() < testCallStartTimeNs) {
            }
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
