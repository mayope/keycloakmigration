package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.validateMockitoUsage
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import de.klg71.keycloakmigration.KoinLogger
import de.klg71.keycloakmigration.changeControl.model.ChangeLog
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import de.klg71.keycloakmigration.changeControl.model.ChangeSetEntry
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
    private val parameters = mutableMapOf<String,String>()

    private val yamlObjectMapper = mock<ObjectMapper> {}
    private val stringEnvSubstitutor = mock<StringEnvSubstitutor> {}

    @Before
    fun setup() {
        reset(yamlObjectMapper)
        whenever(stringEnvSubstitutor.substituteParameters(any())).thenAnswer { it.getArgument(0) as String}
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
        verify(stringEnvSubstitutor, times(2)).substituteParameters(any())
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
    fun testChangesTodo_fileNotFound() {
        val tempChangeLog = "not existing"

        val reader = ChangeFileReader()

        assertThatThrownBy {
            reader.changes(tempChangeLog)
        }.isInstanceOf(FileNotFoundException::class.java)
    }
}
