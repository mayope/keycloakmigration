package de.klg71.keycloakmigration.model

import com.nhaarman.mockitokotlin2.*
import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.changeControl.MigrationChangelogTest
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.user.AddUserAction
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
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

        ChangeSet("test", "author", listOf(testAction),"testRealm","testPath")

        verify(testAction).realm="testRealm"
        verify(testAction).path="testPath"
    }

    @Test
    fun testConstructorRealmNotSet() {
        val testAction = AddUserAction("testRealm","test")
        ChangeSet("test", "author", listOf(testAction),"testRealm1","testPath")

        assertThat(testAction.realm).isEqualTo("testRealm")
        assertThat(testAction.path).isEqualTo("testPath")
    }

    @Test
    fun testHash(){
        val hash = ChangeSet("test", "author", emptyList(),"testRealm1","testPath").hash()
        assertThat(hash).isNotBlank()
    }

    @Test
    fun testHash_ChangeMetadata_id(){
        val hash1 = ChangeSet("test1", "author", emptyList(),"testRealm1","testPath").hash()
        val hash2 = ChangeSet("test2", "author", emptyList(),"testRealm1","testPath").hash()
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun testHash_ChangeMetadata_author(){
        val hash1 = ChangeSet("test", "author1", emptyList(),"testRealm1","testPath").hash()
        val hash2 = ChangeSet("test", "author2", emptyList(),"testRealm1","testPath").hash()
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun testHash_ChangeMetadata_realm(){
        val hash1 = ChangeSet("test", "author", emptyList(),"testRealm1","testPath").hash()
        val hash2 = ChangeSet("test", "author", emptyList(),"testRealm2","testPath").hash()
        assertThat(hash1).isNotEqualTo(hash2)
    }

    @Test
    fun testHash_ChangeMetadata_path(){
        val hash1 = ChangeSet("test", "author", emptyList(),"testRealm","testPath1").hash()
        val hash2 = ChangeSet("test", "author", emptyList(),"testRealm","testPath2").hash()
        assertThat(hash1).isEqualTo(hash2)
    }

    @Test
    fun testHash_ChangeMetadata_actions(){
        val testAction1 = AddUserAction("testRealm1","test")
        val hash1 = ChangeSet("test", "author", listOf(testAction1),"testRealm","testPath1").hash()
        val testAction2 = AddUserAction("testRealm2","test")
        val hash2 = ChangeSet("test", "author", listOf(testAction2),"testRealm","testPath2").hash()
        assertThat(hash1).isNotEqualTo(hash2)
    }
}