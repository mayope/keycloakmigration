package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.model.ChangeLog
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths

/**
 * Reads changelog yaml files
 */
internal class ChangeFileReader : KoinComponent {

    private val yamlObjectMapper by inject<ObjectMapper>(name = "yamlObjectMapper")


    /**
     * Read changelog file and return the list of desired ChangeSets
     */
    internal fun changes(fileName: String): List<ChangeSet> =
            readYamlFile<ChangeLog>(fileName).includes.map {
                if (it.relativeToFile) {
                    readYamlFile<ChangeSet>(parentPath(fileName, it.path).toString()).apply {
                        path = parentPath(fileName, it.path).parent.toString()
                        changes.forEach { action ->
                            action.path = path
                        }
                    }
                } else {
                    readYamlFile<ChangeSet>(it.path).apply {
                        path = File(it.path).absoluteFile.parent.toString()
                        changes.forEach { action ->
                            action.path = path
                        }
                    }
                }
            }

    private fun parentPath(fileName: String, path: String) =
            Paths.get(File(fileName).absoluteFile.parentFile.absolutePath, path)

    private inline fun <reified T> readYamlFile(fileName: String): T {
        if (!File(fileName).exists()) {
            throw RuntimeException("File $fileName not found.")
        }
        return yamlObjectMapper.readValue(FileInputStream(fileName))
    }

}