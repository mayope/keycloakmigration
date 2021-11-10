package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.changeControl.model.ChangeLog
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import de.klg71.keycloakmigration.changeControl.model.ChangeSetEntry
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
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files

class ChangeFileReaderTest : KoinTest {

    private val LOG = LoggerFactory.getLogger(ChangeFileReaderTest::class.java)!!

    private val yamlObjectMapper = mockk<ObjectMapper> {}
    private val stringEnvSubstitutor = mockk<StringEnvSubstitutor> {}

    @Before
    fun setup() {
        clearAllMocks()
        every { stringEnvSubstitutor.substituteParameters(any()) }.answers { firstArg() }
        startKoin {
            logger(KoinLogger(LOG))
            modules(module {
                single(named("yamlObjectMapper")) {
                    yamlObjectMapper
                }
                single {
                    stringEnvSubstitutor
                }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testChangesTodo_onEmptyListReturnEmpty() {
        val tempFile = File.createTempFile("unittest", "ChangeFileReader")
        Files.writeString(tempFile.toPath(), "changelog")


        val changeLog = mockk<ChangeLog>()
        every {
            changeLog.includes
        }.returns(emptyList())


        every { yamlObjectMapper.readValue<ChangeLog>(eq("changelog"), any<TypeReference<ChangeLog>>()) }
            .returns(changeLog)

        val reader = ChangeFileReader()
        val result = reader.changes(tempFile.absolutePath)

        assertThat(result).isEmpty()
    }

    @Test
    fun testChangesTodo_ShouldReturnReadChangeSetNotRelativeToFile() {
        val tempChangeLog = File.createTempFile("changeLog", "ChangeFileReader")
        Files.writeString(tempChangeLog.toPath(), "changelog")
        val tempChangeSet = File.createTempFile("changeSet", "ChangeFileReader")
        Files.writeString(tempChangeSet.toPath(), "changeset")

        val reader = ChangeFileReader()

        val changeSetEntry = mockk<ChangeSetEntry>(relaxed = true)
        every {
            changeSetEntry.path
        } returns (tempChangeSet.absolutePath)
        every { changeSetEntry.relativeToFile }.returns(false)

        val changeLog = mockk<ChangeLog>()
        every {
            changeLog.includes
        }.returns(listOf(changeSetEntry))

        val changeSet = mockk<ChangeSet>(relaxed = true)
        every {
            changeSet.changes
        }.returns(emptyList())

        every {
            yamlObjectMapper.readValue(
                "changelog",
                any<TypeReference<ChangeLog>>()
            )
        }.returns(changeLog)
        every {
            yamlObjectMapper.readValue(
                "changeset",
                any<TypeReference<ChangeSet>>()
            )
        }.returns(changeSet)

        val result = reader.changes(tempChangeLog.absolutePath)

        assertThat(result).isEqualTo(listOf(changeSet))
        verify(atLeast = 2, atMost = 2) {
            stringEnvSubstitutor.substituteParameters(any())
        }
    }

    @Test
    fun testChangesTodo_ShouldReturnReadChangeSetRelativeToFile() {
        val dir = Files.createTempDirectory("ChangeFileReader")
        val tempChangeLog = File.createTempFile("changeLog", "ChangeFileReader", dir.toFile())
        Files.writeString(tempChangeLog.toPath(), "changelog")
        val tempChangeSet = File.createTempFile("changeSet", "ChangeFileReader", dir.toFile())
        Files.writeString(tempChangeSet.toPath(), "changeset")

        val reader = ChangeFileReader()

        val changeSetEntry = mockk<ChangeSetEntry>()
        every {
            changeSetEntry.path
        } returns (tempChangeSet.name)
        every { changeSetEntry.relativeToFile }.returns(true)

        val changeLog = mockk<ChangeLog>()
        every {
            changeLog.includes
        }.returns(listOf(changeSetEntry))

        val changeSet = mockk<ChangeSet>(relaxed = true)
        every {
            changeSet.changes
        }.returns(emptyList())



        every {
            yamlObjectMapper.readValue(
                "changelog",
                any<TypeReference<ChangeLog>>()
            )
        }
            .returns(changeLog)
        every {
            yamlObjectMapper.readValue(
                "changeset",
                any<TypeReference<ChangeSet>>()
            )
        }.returns(changeSet)

        val result = reader.changes(tempChangeLog.absolutePath)

        assertThat(result).isEqualTo(listOf(changeSet))
    }

    @Test
    fun testChangesTodo_fileNotFound() {
        val tempChangeLog = "not existing"

        val reader = ChangeFileReader()

        assertThatThrownBy {
            reader.changes(tempChangeLog)
        }.isInstanceOf(FileNotFoundException::class.java)
    }
}
