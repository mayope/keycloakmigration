package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.actions.ParseException
import de.klg71.keycloakmigration.changeControl.model.ChangeLog
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import org.apache.commons.codec.digest.DigestUtils.sha256Hex
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files.readString
import java.nio.file.Paths

/**
 * Reads changelog yaml files
 */
internal class ChangeFileReader : KoinComponent {

    private val yamlObjectMapper by inject<ObjectMapper>(named("yamlObjectMapper"))
    private val stringEnvSubstitutor by inject<StringEnvSubstitutor>()

    companion object {
        val LOG = LoggerFactory.getLogger(ChangeFileReader::class.java)!!
    }

    /**
     * Read changelog file and return the list of desired ChangeSets
     */
    // Actually Jackson can parse null into ChangeSet through reflection
    @Suppress("SENSELESS_COMPARISON")
    internal fun changes(fileName: String): List<ChangeSet> =
            readYamlFile<ChangeLog>(fileName).includes.map {
                val path = if (it.relativeToFile) {
                    parentPath(fileName, it.path)
                } else {
                    File(it.path).absoluteFile.toPath()
                }
                readYamlFile<ChangeSet>(path.toString()).apply {
                    // senseless comparision see method signature
                    if (changes.any { change -> change == null }) {
                        throw ParseException(
                                "Unable to parse: ${parentPath(fileName,
                                        it.path)}, check formatting or report a bug report!")
                    }
                    changes.forEach { action ->
                        action.path = path.parent.toString()
                    }
                    hash = sha256Hex(readString(path))
                }
            }

    private fun parentPath(fileName: String, path: String) =
            Paths.get(File(fileName).absoluteFile.parentFile.absolutePath, path)

    private inline fun <reified T> readYamlFile(fileName: String): T {
        if (!File(fileName).exists()) {
            throw FileNotFoundException("File $fileName not found.")
        }
        try {
            stringEnvSubstitutor.substituteParameters(readString(Paths.get(fileName))).let {
                return yamlObjectMapper.readValue(it)
            }
        } catch (e: JsonProcessingException) {
            throw ParseException(
                    "Unable to parse: $fileName, check formatting or report a bug report!", e)
        }
    }

}
