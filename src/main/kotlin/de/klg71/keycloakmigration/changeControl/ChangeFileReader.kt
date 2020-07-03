package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.model.ChangeLog
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import org.apache.commons.codec.digest.DigestUtils.sha256Hex
import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.matcher.StringMatcher
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files.readString
import java.nio.file.Paths

/**
 * Reads changelog yaml files
 */
internal class ChangeFileReader(private val failOnUndefinedVariables: Boolean = false,
                                private val warnOnUndefinedVariables: Boolean = true) : KoinComponent {

    private val yamlObjectMapper by inject<ObjectMapper>(named("yamlObjectMapper"))
    private val parameters: Map<String, String> by inject(named("parameters"))

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
                        throw ParseException("Unable to parse: ${parentPath(fileName,
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

    private val parameterSubstitutor = StringSubstitutor(parameters + System.getenv()).apply {
        isEnableUndefinedVariableException = failOnUndefinedVariables
    }

    private fun substituteParameters(value: String) = try {
        parameterSubstitutor.replace(value).also {
            if(warnOnUndefinedVariables) {
                checkForUnreplaced(it)
            }
        }
    } catch (e: IllegalArgumentException) {
        LOG.error("Failed to replace parameters, probably one is missing. See exception below ", e)
        throw e
    }

    private fun checkForUnreplaced(value: String) {
        findMatched(0, value).forEach {
            LOG.warn("Found unresolved variable: $it please check the changelog")
        }
    }

    @Suppress("ReturnCount")
    private fun findMatched(start: Int, value: String): List<String> {
        val prefixMatch = prefixFirstMatch(start, value) ?: return emptyList()
        val suffixMatch = suffixFirstMatch(start + 1, value) ?: return emptyList()

        return listOf(
                value.toCharArray().toList().subList(prefixMatch, suffixMatch + 1).joinToString("")) + findMatched(
                suffixMatch + 1, value)
    }

    private fun prefixFirstMatch(start: Int, value: String): Int? {
        return (start..value.toCharArray().size).firstOrNull {
            parameterSubstitutor.variablePrefixMatcher.matchesAt(value, it)
        }
    }

    private fun suffixFirstMatch(start: Int, value: String): Int? {
        return (start..value.toCharArray().size).firstOrNull {
            parameterSubstitutor.variableSuffixMatcher.matchesAt(value, it)
        }
    }

    private fun StringMatcher.matchesAt(value: String, position: Int) =
            isMatch(value.toCharArray(), position, 0, value.length) > 0

    private inline fun <reified T> readYamlFile(fileName: String): T {
        if (!File(fileName).exists()) {
            throw FileNotFoundException("File $fileName not found.")
        }
        try {
            substituteParameters(readString(Paths.get(fileName))).let {
                return yamlObjectMapper.readValue(it)
            }
        } catch (e: JsonProcessingException) {
            throw ParseException("Unable to parse: $fileName, check formatting or report a bug report!", e)
        }
    }

}
