package de.klg71.keycloakmigration.changeControl

import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.changeControl.actions.ActionTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.Realm
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory

class RealmCheckerTest : KoinTest {

    private val logger = LoggerFactory.getLogger(ActionTest::class.java)!!
    private val client = mockk<KeycloakClient>(relaxed = true)

    @Before
    fun setup() {
        clearAllMocks()
        startKoin {
            logger(KoinLogger(logger))
            modules(module {
                single { client }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testCheck() {
        val realmChecker = RealmChecker()
        val mockRealm = mockk<Realm>()
        every {
            mockRealm.id
        } returns "test"

        every {
            client.realms()
        }.returns(listOf(mockRealm))

        assertThat(realmChecker.check("test")).isEqualTo("test")

        verify {
            client.realms()
        }
    }

    @Test
    fun testCheckCache() {
        val realmChecker = RealmChecker(listOf("test"))
        val mockRealm = mockk<Realm>()
        every {
            mockRealm.id
        } returns "test"

        every {
            client.realms()
        }.returns(listOf(mockRealm))

        assertThat(realmChecker.check("test")).isEqualTo("test")

        verify(atMost = 0, atLeast = 0) {
            client.realms()
        }
    }

    @Test
    fun testCheckException() {
        val realmChecker = RealmChecker()

        every {
            client.realms()
        }.returns(listOf())

        assertThatThrownBy {
            realmChecker.check("test")
        }.isInstanceOf(MigrationException::class.java).hasMessage("Realm with id: test does not exist!")

        verify(atMost = 1, atLeast = 1) {
            client.realms()
        }
    }
}
