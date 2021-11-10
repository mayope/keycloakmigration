package de.klg71.keycloakmigration.model

import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.changeControl.MigrationChangelogTest
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.user.AddUserAction
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory

class ChangeSetTest : KoinTest {

    val LOG = LoggerFactory.getLogger(MigrationChangelogTest::class.java)

    @Before
    fun setup() {
        startKoin {
            logger(KoinLogger(LOG))
            modules(module {
            })
        }
        clearAllMocks()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testConstructorRealmSet() {
        val testAction = object:Action(){
            override fun execute() {
                // nothing todo
            }

            override fun undo() {
                // nothing todo
            }

            override fun name(): String ="testAction"
        }

        ChangeSet(
            "test", "author",
            listOf(testAction), "testRealm", "testPath"
        )

        assertThat(testAction.realm).isEqualTo("testRealm")
        assertThat(testAction.path).isEqualTo("testPath")
    }

    @Test
    fun testConstructorRealmNotSet() {
        val testAction = AddUserAction("testRealm", "test")
        ChangeSet(
            "test", "author",
            listOf(testAction), "testRealm1", "testPath"
        )

        assertThat(testAction.realm).isEqualTo("testRealm")
        assertThat(testAction.path).isEqualTo("testPath")
    }
}
