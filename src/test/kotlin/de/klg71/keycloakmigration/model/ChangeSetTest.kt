package de.klg71.keycloakmigration.keycloakapi.model

import com.nhaarman.mockitokotlin2.*
import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.changeControl.MigrationChangelogTest
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.user.AddUserAction
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory

class ChangeSetTest :KoinTest{

    val LOG = LoggerFactory.getLogger(MigrationChangelogTest::class.java)

    @Before
    fun setup() {
        startKoin {
            logger(KoinLogger(LOG))
            modules(module {
            })
        }
    }

    @After
    fun tearDown() {
        validateMockitoUsage()
        stopKoin()
    }

    @Test
    fun testConstructorRealmSet() {
        val testAction = mock<Action> {
        }

        ChangeSet("test", "author",
                listOf(testAction), "testRealm", "testPath")

        verify(testAction).realm="testRealm"
        verify(testAction).path="testPath"
    }

    @Test
    fun testConstructorRealmNotSet() {
        val testAction = AddUserAction("testRealm","test")
        ChangeSet("test", "author",
                listOf(testAction), "testRealm1", "testPath")

        assertThat(testAction.realm).isEqualTo("testRealm")
        assertThat(testAction.path).isEqualTo("testPath")
    }
}
