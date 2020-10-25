package de.klg71.keycloakmigration.changeControl

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.validateMockitoUsage
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.changeControl.MigrationChangelog.Companion.migrationAttributeName
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.User
import de.klg71.keycloakmigration.keycloakapi.model.UserAccess
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
import java.util.UUID.randomUUID

internal class MigrationChangelogTest : KoinTest {

    private val LOG = LoggerFactory.getLogger(MigrationChangelogTest::class.java)

    private val client = mock<KeycloakClient>()

    @Before
    fun setup() {
        reset(client)
        startKoin {
            logger(KoinLogger(LOG))
            modules(module {
                single { client }
            })
        }
    }

    @After
    fun tearDown() {
        validateMockitoUsage()
        stopKoin()
    }

    @Test
    fun testChangesTodo_onEmptyListReturnEmpty() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> { }

        whenever(client.user(migrationUserId, realm)).thenReturn(user)
        val result = changelog.changesTodo(emptyList())

        verify(client, times(2)).user(migrationUserId, realm)

        assertThat(result).isEmpty()
    }

    @Test
    fun testChangesTodo_onNullMigrationHashesReturnFullList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> { }

        whenever(client.user(migrationUserId, realm)).thenReturn(user)
        val result = changelog.changesTodo(listOf(mockChangeSet(), mockChangeSet()))

        verify(client, times(2)).user(migrationUserId, realm)

        assertThat(result).hasSize(2)
    }

    @Test
    fun testChangesTodo_onNullAttributesReturnFullList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> {
            on { attributes } doReturn null
        }

        whenever(client.user(migrationUserId, realm)).thenReturn(user)
        val result = changelog.changesTodo(listOf(mockChangeSet(), mockChangeSet()))

        verify(client, times(2)).user(migrationUserId, realm)

        assertThat(result).hasSize(2)
    }

    @Test
    fun testChangesTodo_onEmptyAttributesReturnFullList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> {
            on { attributes } doReturn mapOf()
        }

        whenever(client.user(migrationUserId, realm)).thenReturn(user)
        val result = changelog.changesTodo(listOf(mockChangeSet(), mockChangeSet()))

        verify(client, times(2)).user(migrationUserId, realm)

        assertThat(result).hasSize(2)
    }

    @Test
    fun testChangesTodo_onEmptyMigrationHashesReturnFullList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> {
            on { attributes } doReturn mapOf(migrationAttributeName to listOf())
        }

        whenever(client.user(migrationUserId, realm)).thenReturn(user)
        val result = changelog.changesTodo(listOf(mockChangeSet(), mockChangeSet()))

        verify(client, times(2)).user(migrationUserId, realm)

        assertThat(result).hasSize(2)
    }

    @Test
    fun testChangesTodo_onHashMismatchFail() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> {
            on { id } doReturn migrationUserId
            on { username } doReturn "username"
            on { attributes } doReturn mapOf(migrationAttributeName to listOf("v2/1/rightHash"))
        }

        val changeSet = mockChangeSet(testHash = "wrongHash", testId = "testId")

        whenever(client.user(migrationUserId, realm)).thenReturn(user)
        assertThatThrownBy { changelog.changesTodo(listOf(changeSet)) }
                .hasMessage("Invalid hash expected: rightHash (remote) got wrongHash (local) in migration: testId")
                .isInstanceOf(MigrationException::class.java)

    }

    @Test
    fun testChangesTodo_onPresentHashMigrateV2() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> {
            on { id } doReturn migrationUserId
            on { username } doReturn "username"
            on { attributes } doReturn mapOf(migrationAttributeName to listOf("rightHash"))
        }
        val v2User = mock<User> {
            on { id } doReturn migrationUserId
            on { username } doReturn "username"
            on { attributes } doReturn mapOf(migrationAttributeName to listOf("v2/1/rightHash"))
        }

        val changeSet = mockChangeSet(testHash = "rightHash")

        var first = true

        whenever(client.user(migrationUserId, realm)).thenAnswer {
            if (first) {
                first = false
                return@thenAnswer user
            } else {
                return@thenAnswer v2User
            }

        }
        val result = changelog.changesTodo(listOf(changeSet))

        assertThat(result).isEmpty()
    }


    @Test
    fun testChangesTodo_onPresentSkip() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val v2User = mock<User> {
            on { id } doReturn migrationUserId
            on { username } doReturn "username"
            on { attributes } doReturn mapOf(migrationAttributeName to listOf("v2/1/rightHash"))
        }

        val changeSet = mockChangeSet(testHash = "rightHash")

        whenever(client.user(migrationUserId, realm)).thenReturn(v2User)
        val result = changelog.changesTodo(listOf(changeSet))

        assertThat(result).isEmpty()
    }

    @Test
    fun testChangesTodo_filterChangeSetBasedOnEnabledFlag() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> {
            on { id } doReturn migrationUserId
            on { username } doReturn "username"
            on { attributes } doReturn mapOf(migrationAttributeName to listOf())
        }

        val changeSetBefore = mockChangeSet(testHash = "hashBefore")
        val disabledChangeSet = mockChangeSet(isEnabled = false)
        val changeSetAfter = mockChangeSet(testHash = "hashAfter")

        whenever(client.user(migrationUserId, realm)).thenReturn(user)
        val result = changelog.changesTodo(listOf(changeSetBefore, disabledChangeSet, changeSetAfter))

        assertThat(result).containsExactly(changeSetBefore, changeSetAfter)
    }

    @Test
    fun testChangesTodo_filterChangeSetBasedOnEnabledFlagWithNewChangeSet() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> {
            on { id } doReturn migrationUserId
            on { username } doReturn "username"
            on { attributes } doReturn mapOf(migrationAttributeName to listOf("v2/1/hashBefore", "v2/2/hashAfter"))
        }

        val changeSetBefore = mockChangeSet(testHash = "hashBefore")
        val disabledChangeSet = mockChangeSet(isEnabled = false)
        val changeSetAfter = mockChangeSet(testHash = "hashAfter", isEnabled = true)
        val newChangeSet = mockChangeSet(testHash = "hashNew")

        whenever(client.user(migrationUserId, realm)).thenReturn(user)
        val result = changelog.changesTodo(listOf(changeSetBefore, disabledChangeSet, changeSetAfter, newChangeSet))

        assertThat(result).containsExactly(newChangeSet)
    }


    @Test
    fun testChangesTodo_WrongOrder() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mock<User> {
            on { id } doReturn migrationUserId
            on { username } doReturn "username"
            on { attributes } doReturn mapOf(migrationAttributeName to listOf("v2/2/hashAfter", "v2/1/hashBefore"))
        }

        val changeSetBefore = mockChangeSet(testHash = "hashBefore")
        val changeSetAfter = mockChangeSet(testHash = "hashAfter")

        whenever(client.user(migrationUserId, realm)).thenReturn(user)
        val result = changelog.changesTodo(listOf(changeSetBefore, changeSetAfter))

        assertThat(result).isEmpty()
    }

    @Test
    fun writeChangeToUser_shouldCreateMigrationsAttributeOnMissing() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val userBefore = User(migrationUserId, 0L, "test", true, true, null, 0L, false,
                UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null, null)

        val changeSet = mockChangeSet(testHash = "hash")
        val userAfter = User(migrationUserId, 0L, "test", true, true, mapOf(migrationAttributeName to listOf("hash")),
                0L, false, UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null,
                null)

        whenever(client.user(migrationUserId, realm)).thenReturn(userBefore)
        changelog.writeChangeToUser(changeSet)
        verify(client).updateUser(eq(migrationUserId), eq(userAfter), eq(realm))
    }

    @Test
    fun writeChangeToUser_shouldAddMigrationsAttributeOnEmptyList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val userBefore = User(migrationUserId, 0L, "test", true, true, mapOf(migrationAttributeName to emptyList()), 0L,
                false, UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null,
                null)

        val changeSet = mockChangeSet(testHash = "hash")
        val userAfter = User(migrationUserId, 0L, "test", true, true, mapOf(migrationAttributeName to listOf("hash")),
                0L, false, UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null,
                null)

        whenever(client.user(migrationUserId, realm)).thenReturn(userBefore)
        changelog.writeChangeToUser(changeSet)
        verify(client).updateUser(eq(migrationUserId), eq(userAfter), eq(realm))
    }

    @Test
    fun writeChangeToUser_shouldAddMigrationsAttributeOnPresentList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val userBefore = User(migrationUserId, 0L, "test", true, true,
                mapOf(migrationAttributeName to listOf("hashBefore")), 0L, false,
                UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null, null)

        val changeSet = mockChangeSet(testHash = "hash")
        val userAfter = User(migrationUserId, 0L, "test", true, true,
                mapOf(migrationAttributeName to listOf("hashBefore", "hash")), 0L, false,
                UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null, null)

        whenever(client.user(migrationUserId, realm)).thenReturn(userBefore)
        changelog.writeChangeToUser(changeSet)
        verify(client).updateUser(eq(migrationUserId), eq(userAfter), eq(realm))
    }

    private fun mockChangeSet(testHash: String = "", isEnabled: Boolean = true, testId: String = ""): ChangeSet =
            mock {
                on { enabled } doReturn isEnabled
                on { hash() } doReturn testHash
                on { id } doReturn testId
            }
}
