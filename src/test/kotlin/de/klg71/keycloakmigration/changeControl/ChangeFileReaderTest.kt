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
import java.nio.file.Files

class ChangeFileReaderTest : KoinTest {

    val LOG = LoggerFactory.getLogger(ChangeFileReaderTest::class.java)!!
    val parameters = mutableMapOf<String,String>()

    private val yamlObjectMapper = mock<ObjectMapper> {
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
                single(named("parameters")) {
                    parameters
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
        Files.writeString(tempFile.toPath(),"changelog")


        val changeLog = mock<ChangeLog> {
            on { includes } doReturn emptyList()
        }


        whenever(yamlObjectMapper.readValue<ChangeLog>(eq("changelog"), any<TypeReference<ChangeLog>>()))
                .thenReturn(changeLog)

        val reader = ChangeFileReader()
        val result = reader.changes(tempFile.absolutePath)

        assertThat(result).isEmpty()
    }

    @Test
    fun testChangesTodo_ShouldReturnReadChangeSetNotRelativeToFile() {
        val tempChangeLog = File.createTempFile("changeLog", "ChangeFileReader")
        Files.writeString(tempChangeLog.toPath(),"changelog")
        val tempChangeSet = File.createTempFile("changeSet", "ChangeFileReader")
        Files.writeString(tempChangeSet.toPath(),"changeset")

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

        whenever(yamlObjectMapper.readValue<ChangeLog>(eq("changelog"),
                any<TypeReference<ChangeLog>>()))
                .thenReturn(changeLog)
        whenever(yamlObjectMapper.readValue<ChangeSet>(eq("changeset"),
                any<TypeReference<ChangeSet>>()))
                .thenReturn(changeSet)

        val result = reader.changes(tempChangeLog.absolutePath)

        assertThat(result).isEqualTo(listOf(changeSet))
    }

    @Test
    fun testChangesTodo_ShouldReturnReadChangeSetRelativeToFile() {
        val dir = Files.createTempDirectory("ChangeFileReader")
        val tempChangeLog = File.createTempFile("changeLog", "ChangeFileReader", dir.toFile())
        Files.writeString(tempChangeLog.toPath(),"changelog")
        val tempChangeSet = File.createTempFile("changeSet", "ChangeFileReader", dir.toFile())
        Files.writeString(tempChangeSet.toPath(),"changeset")

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

        whenever(yamlObjectMapper.readValue<ChangeLog>(eq("changelog"),
                any<TypeReference<ChangeLog>>()))
                .thenReturn(changeLog)
        whenever(yamlObjectMapper.readValue<ChangeSet>(eq("changeset"),
                any<TypeReference<ChangeSet>>()))
                .thenReturn(changeSet)

        val result = reader.changes(tempChangeLog.absolutePath)

        assertThat(result).isEqualTo(listOf(changeSet))
    }


    @Test
    fun testChangesTodo_ParameterSubstitution() {
        val dir = Files.createTempDirectory("ChangeFileReader")
        val tempChangeLog = File.createTempFile("changeLog", "ChangeFileReader", dir.toFile())
        Files.writeString(tempChangeLog.toPath(),"changelog")
        val tempChangeSet = File.createTempFile("changeSet", "ChangeFileReader", dir.toFile())
        Files.writeString(tempChangeSet.toPath(),"\${TEST_PARAM}")
        parameters["TEST_PARAM"] = "TEST_VALUE"

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

        whenever(yamlObjectMapper.readValue<ChangeLog>(eq("changelog"),
                any<TypeReference<ChangeLog>>()))
                .thenReturn(changeLog)
        whenever(yamlObjectMapper.readValue<ChangeSet>(eq(parameters["TEST_PARAM"]),
                any<TypeReference<ChangeSet>>()))
                .thenReturn(changeSet)

        val result = reader.changes(tempChangeLog.absolutePath)

        assertThat(result).isEqualTo(listOf(changeSet))
    }
}