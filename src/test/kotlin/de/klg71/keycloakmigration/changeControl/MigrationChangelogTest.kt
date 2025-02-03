package de.klg71.keycloakmigration.changeControl

import de.klg71.keycloakmigration.changeControl.MigrationChangelog.Companion.migrationAttributeName
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.model.User
import de.klg71.keycloakmigration.keycloakapi.model.UserAccess
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
import org.koin.logger.SLF4JLogger
import org.koin.test.KoinTest
import java.util.UUID.randomUUID

internal class MigrationChangelogTest : KoinTest {

    private val client = mockk<KeycloakClient>(relaxed = true)

    @Before
    fun setup() {
        clearAllMocks()
        startKoin {
            logger(SLF4JLogger())
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
    fun testChangesTodo_onEmptyListReturnEmpty() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mockk<User>(relaxed = true) { }
        every { client.user(migrationUserId, realm) }.returns(user)

        val result = changelog.changesTodo(emptyList())

        verify(atLeast = 2, atMost = 2) {
            client.user(migrationUserId, realm)
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun testChangesTodo_onNullMigrationHashesReturnFullList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mockk<User>(relaxed = true) { }
        every { client.user(migrationUserId, realm) }.returns(user)
        val result = changelog.changesTodo(listOf(mockChangeSet(), mockChangeSet()))

        verify(atLeast = 2, atMost = 2) {
            client.user(migrationUserId, realm)
        }

        assertThat(result).hasSize(2)
    }

    @Test
    fun testChangesTodo_onNullAttributesReturnFullList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mockk<User> { }
        every { client.user(migrationUserId, realm) }.returns(user)
        every { user.attributes }.returns(null)

        val result = changelog.changesTodo(listOf(mockChangeSet(), mockChangeSet()))

        verify(atLeast = 2, atMost = 2) {
            client.user(migrationUserId, realm)
        }

        assertThat(result).hasSize(2)
    }

    @Test
    fun testChangesTodo_onEmptyAttributesReturnFullList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mockk<User> { }
        every { client.user(migrationUserId, realm) }.returns(user)
        every { user.attributes }.returns(emptyMap())

        val result = changelog.changesTodo(listOf(mockChangeSet(), mockChangeSet()))


        verify(atLeast = 2, atMost = 2) {
            client.user(migrationUserId, realm)
        }

        assertThat(result).hasSize(2)
    }

    @Test
    fun testChangesTodo_onEmptyMigrationHashesReturnFullList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)

        val user = mockk<User> { }
        every { client.user(migrationUserId, realm) }.returns(user)
        every { user.attributes }.returns(mapOf(migrationAttributeName to listOf()))

        val result = changelog.changesTodo(listOf(mockChangeSet(), mockChangeSet()))

        verify(atLeast = 2, atMost = 2) {
            client.user(migrationUserId, realm)
        }

        assertThat(result).hasSize(2)
    }

    @Test
    fun testChangesTodo_onHashMismatchFail() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)

        val user = mockk<User> { }
        every { client.user(migrationUserId, realm) }.returns(user)
        every { user.username }.returns("username")
        every { user.attributes }.returns(mapOf(migrationAttributeName to listOf("v2/1/rightHash")))

        val changeSet = mockChangeSet(testHash = "wrongHash", testId = "testId")

        assertThatThrownBy { changelog.changesTodo(listOf(changeSet)) }
            .hasMessage("Invalid hash expected: rightHash (remote) got wrongHash (local) in migration: testId")
            .isInstanceOf(MigrationException::class.java)

    }

    @Test
    fun testChangesTodo_onPresentHashMigrateV2() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mockk<User>(relaxed = true) { }
        every { user.id }.returns(migrationUserId)
        every { client.user(migrationUserId, realm) }.returns(user)
        every { user.username }.returns("username")
        every { user.attributes }.returns(mapOf(migrationAttributeName to listOf("rightHash")))

        val v2User = mockk<User>(relaxed = true) { }
        every { v2User.id }.returns(migrationUserId)
        every { v2User.username }.returns("username")
        every { v2User.attributes }.returns(mapOf(migrationAttributeName to listOf("v2/1/rightHash")))


        val changeSet = mockChangeSet(testHash = "rightHash")

        every { client.user(migrationUserId, realm) }.returnsMany(user, v2User)
        val result = changelog.changesTodo(listOf(changeSet))

        assertThat(result).isEmpty()
    }

    @Test
    fun testChangesTodo_onPresentSkip() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mockk<User> { }
        every { client.user(migrationUserId, realm) }.returns(user)
        every { user.username }.returns("username")
        every { user.attributes }.returns(mapOf(migrationAttributeName to listOf("v2/1/rightHash")))

        val changeSet = mockChangeSet(testHash = "rightHash")

        val result = changelog.changesTodo(listOf(changeSet))

        assertThat(result).isEmpty()
    }

    @Test
    fun testChangesTodo_filterChangeSetBasedOnEnabledFlag() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mockk<User> { }
        every { client.user(migrationUserId, realm) }.returns(user)
        every { user.username }.returns("username")
        every { user.attributes }.returns(mapOf(migrationAttributeName to listOf()))

        val changeSetBefore = mockChangeSet(testHash = "hashBefore")
        val disabledChangeSet = mockChangeSet(isEnabled = false)
        val changeSetAfter = mockChangeSet(testHash = "hashAfter")

        val result = changelog.changesTodo(listOf(changeSetBefore, disabledChangeSet, changeSetAfter))

        assertThat(result).containsExactly(changeSetBefore, changeSetAfter)
    }

    @Test
    fun testChangesTodo_filterChangeSetBasedOnEnabledFlagWithNewChangeSet() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mockk<User> { }
        every { client.user(migrationUserId, realm) }.returns(user)
        every { user.username }.returns("username")
        every { user.attributes }.returns(mapOf(migrationAttributeName to listOf("v2/1/hashBefore", "v2/2/hashAfter")))

        val changeSetBefore = mockChangeSet(testHash = "hashBefore")
        val disabledChangeSet = mockChangeSet(isEnabled = false)
        val changeSetAfter = mockChangeSet(testHash = "hashAfter", isEnabled = true)
        val newChangeSet = mockChangeSet(testHash = "hashNew")

        val result = changelog.changesTodo(listOf(changeSetBefore, disabledChangeSet, changeSetAfter, newChangeSet))

        assertThat(result).containsExactly(newChangeSet)
    }

    @Test
    fun testChangesTodo_WrongOrder() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val user = mockk<User> { }
        every { client.user(migrationUserId, realm) }.returns(user)
        every { user.username }.returns("username")
        every { user.attributes }.returns(mapOf(migrationAttributeName to listOf("v2/2/hashAfter", "v2/1/hashBefore")))

        val changeSetBefore = mockChangeSet(testHash = "hashBefore")
        val changeSetAfter = mockChangeSet(testHash = "hashAfter")

        val result = changelog.changesTodo(listOf(changeSetBefore, changeSetAfter))

        assertThat(result).isEmpty()
    }

    @Test
    fun writeChangeToUser_shouldCreateMigrationsAttributeOnMissing() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val userBefore = User(
            migrationUserId, 0L, "test", true, true, null, 0L, false,
            UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null, null
        )

        val changeSet = mockChangeSet(testHash = "hash")
        val userAfter = User(
            migrationUserId, 0L, "test", true, true, mapOf(migrationAttributeName to listOf("v2/0/hash")),
            0L, false, UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null,
            null
        )

        every { client.user(migrationUserId, realm) }.returns(userBefore)
        changelog.writeChangeToUser(changeSet)
        verify {
            client.updateUser(migrationUserId, userAfter, realm)
        }
    }

    @Test
    fun writeChangeToUser_shouldAddMigrationsAttributeOnEmptyList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val userBefore = User(
            migrationUserId, 0L, "test", true, true, mapOf(migrationAttributeName to emptyList()), 0L,
            false, UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null,
            null
        )

        val changeSet = mockChangeSet(testHash = "hash")
        val userAfter = User(
            migrationUserId, 0L, "test", true, true, mapOf(migrationAttributeName to listOf("v2/0/hash")),
            0L, false, UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null,
            null
        )

        every { client.user(migrationUserId, realm) }.returns(userBefore)
        changelog.writeChangeToUser(changeSet)
        verify {
            client.updateUser(migrationUserId, userAfter, realm)
        }
    }

    @Test
    fun writeChangeToUser_shouldAddMigrationsAttributeOnPresentList() {
        val migrationUserId = randomUUID()
        val realm = "test"
        val changelog = MigrationChangelog(migrationUserId, realm)
        val userBefore = User(
            migrationUserId, 0L, "test", true, true,
            mapOf(migrationAttributeName to listOf("v2/0/hashBefore")), 0L, false,
            UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null, null
        )

        val changeSet = mockChangeSet(testHash = "hash")
        val userAfter = User(
            migrationUserId, 0L, "test", true, true,
            mapOf(migrationAttributeName to listOf("v2/0/hashBefore", "v2/1/hash")), 0L, false,
            UserAccess(false, false, false, false, false), emptyList(), emptyList(), null, "test", null, null
        )

        every { client.user(migrationUserId, realm) }.returns(userBefore)
        changelog.writeChangeToUser(changeSet)
        verify {
            client.updateUser(migrationUserId, userAfter, realm)
        }
    }

    private fun mockChangeSet(testHash: String = "", isEnabled: Boolean = true, testId: String = ""): ChangeSet =
        mockk<ChangeSet>().apply {
            every { enabled } returns isEnabled
            every { hash() } returns testHash
            every { id } returns testId
        }
}
