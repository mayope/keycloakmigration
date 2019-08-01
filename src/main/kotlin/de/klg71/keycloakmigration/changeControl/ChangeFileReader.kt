package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.model.ChangeLog
import de.klg71.keycloakmigration.model.ChangeSet
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import java.io.File
import java.nio.file.Paths

/**
 * Reads changelog yaml files
 */
internal class ChangeFileReader : KoinComponent {

    private val yamlObjectMapper by inject<ObjectMapper>(named("yamlObjectMapper"))


    /**
     * Read changelog file and return the list of desired ChangeSets
     */
    // Actually Jackson can parse null into ChangeSet
    @Suppress("SENSELESS_COMPARISON")
    internal fun changes(fileName: String): List<ChangeSet> =
            readYamlFile<ChangeLog>(fileName).includes.map {
                val path = if (it.relativeToFile) {
                    parentPath(fileName, it.path)
                } else {
                    File(it.path).absoluteFile.toPath()
                }
                readYamlFile<ChangeSet>(path.toString()).apply {
                    if (changes.any { change -> change == null }) {
                        throw ParseException("Unable to parse: ${parentPath(fileName, it.path)}, check formatting or report a bug report!")
                    }
                    changes.forEach { action ->
                        action.path = path.parent.toString()
                    }
                }
            }

    private fun parentPath(fileName: String, path: String) =
            Paths.get(File(fileName).absoluteFile.parentFile.absolutePath, path)

    private inline fun <reified T> readYamlFile(fileName: String): T {
        if (!File(fileName).exists()) {
            throw RuntimeException("File $fileName not found.")
        }
        try {
            return yamlObjectMapper.readValue(File(fileName).toURI().toURL())
        } catch (e: JsonProcessingException) {
            throw ParseException("Unable to parse: $fileName, check formatting or report a bug report!", e)
        }
    }

}