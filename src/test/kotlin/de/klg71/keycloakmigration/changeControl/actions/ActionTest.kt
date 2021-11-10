package de.klg71.keycloakmigration.changeControl.actions

import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.validateMockitoUsage
import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.changeControl.RealmChecker
import de.klg71.keycloakmigration.keycloakapi.model.Realm
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito
import org.slf4j.LoggerFactory

class ActionTest : KoinTest {

    class MockAction(private val throwException: Boolean = false,
                     private val undoOnException: Boolean = false,
                     private val exceptionInUndo: Boolean = false,
                     realm: String? = null) : Action(realm) {
        var undoCalled = false
        override fun execute() {
            if (undoOnException) {
                setExecuted()
            }
            if (throwException) {
                throw MigrationException("test")
            }
        }

        override fun undo() {
            undoCalled = true
            if (exceptionInUndo) {
                throw MigrationException("undoTest")
            }
        }

        override fun name() = "testAction"

        fun callRealm() = realm()
    }


    private val logger = LoggerFactory.getLogger(ActionTest::class.java)!!
    private val client = mock<KeycloakClient>()
    private val realmChecker = mock<RealmChecker>()

    @Before
    fun setup() {
        reset(client, realmChecker)
        startKoin {
            logger(KoinLogger(logger))
            modules(module {
                single { client }
                single {realmChecker}
            })
        }
    }

    @After
    fun tearDown() {
        validateMockitoUsage()
        stopKoin()
    }

    @Test
    fun testExecuted() {
        val mockAction = MockAction()
        mockAction.executeIt()
        assertThat(mockAction.undoCalled).isFalse()
    }

    @Test
    fun testUndoOnException() {
        val mockAction = MockAction(true)

        assertThatThrownBy {
            mockAction.executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("test")

        assertThat(mockAction.undoCalled).isFalse()
    }

    @Test
    fun testUndoOnExceptionSet() {
        val mockAction = MockAction(throwException = true, undoOnException = true)

        assertThatThrownBy {
            mockAction.executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("test")

        assertThat(mockAction.undoCalled).isTrue()
    }

    @Test
    fun testUndoOnExceptionSetExceptionInUndo() {
        val mockAction = MockAction(throwException = true, undoOnException = true, exceptionInUndo = true)

        assertThatThrownBy {
            mockAction.executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessage("undoTest")

        assertThat(mockAction.undoCalled).isTrue()
    }

    @Test
    fun testRealm_notSet() {
        val mockAction = MockAction()

        assertThatThrownBy {
            mockAction.callRealm()
        }.isInstanceOf(ParseException::class.java)
                .hasMessage("Realm is null for testAction, either provide it in the change or the changeset!")
    }

    @Test
    fun testRealm_SetReachable() {
        val mockAction = MockAction(realm = "test")
        val mockRealm = mockk<Realm>()
        every {
            mockRealm.id
        }.returns("test")

        Mockito.`when`(client.realms()).thenReturn(listOf(mockRealm))

        assertThat(mockAction.callRealm()).isEqualTo("test")
    }

    @Test
    fun testRealm_SetUnReachable() {
        val mockAction = MockAction(realm = "test")

        Mockito.`when`(realmChecker.check("test")).thenThrow(MigrationException("Realm does not exist"))

        assertThatThrownBy {
            mockAction.callRealm()
        }.isInstanceOf(MigrationException::class.java).hasMessage("Realm does not exist")
    }
}
