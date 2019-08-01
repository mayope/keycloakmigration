package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.*
import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.model.ChangeLog
import de.klg71.keycloakmigration.model.ChangeSet
import de.klg71.keycloakmigration.model.ChangeSetEntry
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
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files

class ChangeFileReaderTest : KoinTest {

    val LOG = LoggerFactory.getLogger(ChangeFileReaderTest::class.java)

    private val yamlObjectMapper = mock<ObjectMapper>() {
    }

    @Before
    fun setup() {
        reset(yamlObjectMapper)
        startKoin {
            logger(KoinLogger(LOG))
            modules(module {
                single(named("yamlObjectMapper")) {
                    yamlObjectMapper
                }
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
        val tempFile = File.createTempFile("unittest", "ChangeFileReader")


        val changeLog = mock<ChangeLog> {
            on { includes } doReturn emptyList()
        }


        whenever(yamlObjectMapper.readValue<ChangeLog>(eq(tempFile.toURI().toURL()), any<TypeReference<ChangeLog>>()))
                .thenReturn(changeLog)

        val reader = ChangeFileReader()
        val result = reader.changes(tempFile.absolutePath)

        assertThat(result).isEmpty()
    }

    @Test
    fun testChangesTodo_ShouldReturnReadChangeSetNotRelativeToFile() {
        val tempChangeLog = File.createTempFile("changeLog", "ChangeFileReader")
        val tempChangeSet = File.createTempFile("changeSet", "ChangeFileReader")

        val reader = ChangeFileReader()

        val changeSetEntry = mock<ChangeSetEntry> {
            on { path } doReturn tempChangeSet.absolutePath
            on { relativeToFile } doReturn false
        }
        val changeLog = mock<ChangeLog> {
            on { includes } doReturn listOf(changeSetEntry)
        }

        val changeSet = mock<ChangeSet> {
            on { changes } doReturn emptyList()
        }

        whenever(yamlObjectMapper.readValue<ChangeLog>(eq(tempChangeLog.toURI().toURL()), any<TypeReference<ChangeLog>>()))
                .thenReturn(changeLog)
        whenever(yamlObjectMapper.readValue<ChangeSet>(eq(tempChangeSet.toURI().toURL()), any<TypeReference<ChangeSet>>()))
                .thenReturn(changeSet)

        val result = reader.changes(tempChangeLog.absolutePath)

        assertThat(result).isEqualTo(listOf(changeSet))
    }

    @Test
    fun testChangesTodo_ShouldReturnReadChangeSetRelativeToFile() {
        val dir = Files.createTempDirectory("ChangeFileReader")
        val tempChangeLog = File.createTempFile("changeLog", "ChangeFileReader", dir.toFile())
        val tempChangeSet = File.createTempFile("changeSet", "ChangeFileReader", dir.toFile())

        val reader = ChangeFileReader()

        val changeSetEntry = mock<ChangeSetEntry> {
            on { path } doReturn tempChangeSet.name
            on { relativeToFile } doReturn true
        }
        val changeLog = mock<ChangeLog> {
            on { includes } doReturn listOf(changeSetEntry)
        }

        val changeSet = mock<ChangeSet> {
            on { changes } doReturn emptyList()
        }

        whenever(yamlObjectMapper.readValue<ChangeLog>(eq(tempChangeLog.toURI().toURL()), any<TypeReference<ChangeLog>>()))
                .thenReturn(changeLog)
        whenever(yamlObjectMapper.readValue<ChangeSet>(eq(tempChangeSet.toURI().toURL()), any<TypeReference<ChangeSet>>()))
                .thenReturn(changeSet)

        val result = reader.changes(tempChangeLog.absolutePath)

        assertThat(result).isEqualTo(listOf(changeSet))
    }
}