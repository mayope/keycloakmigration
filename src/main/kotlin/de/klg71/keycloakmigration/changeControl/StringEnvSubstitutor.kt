package de.klg71.keycloakmigration.changeControl

import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.matcher.StringMatcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

internal class StringEnvSubstitutor(private val failOnUndefinedVariables: Boolean = false,
                                    private val warnOnUndefinedVariables: Boolean = true) : KoinComponent {

    private val parameters: Map<String, String> by inject(named("parameters"))

    private val parameterSubstitutor = StringSubstitutor(parameters + System.getenv()).apply {
        isEnableUndefinedVariableException = failOnUndefinedVariables
    }

    fun substituteParameters(value: String): String = try {
        parameterSubstitutor.replace(value).also {
            if (warnOnUndefinedVariables) {
                checkForUnreplaced(it)
            }
        }
    } catch (e: IllegalArgumentException) {
        ChangeFileReader.LOG.error("Failed to replace parameters, probably one is missing. See exception below ", e)
        throw e
    }

    private fun checkForUnreplaced(value: String) {
        findMatched(0, value).forEach {
            ChangeFileReader.LOG.warn("Found unresolved variable: $it please check the changelog")
        }
    }

    @Suppress("ReturnCount")
    private fun findMatched(start: Int, value: String): List<String> {
        val prefixMatch = prefixFirstMatch(start, value) ?: return emptyList()
        val suffixMatch = suffixFirstMatch(start + 1, value) ?: return emptyList()

        val foundVariable = value.toCharArray().toList().subList(prefixMatch, suffixMatch + 1).joinToString("")
        return listOf(foundVariable) + findMatched(suffixMatch + 1, value)
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
}
