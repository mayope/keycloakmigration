package de.klg71.keycloakmigration.keycloakapi.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.UUID

data class Flow(val id: UUID,
    val alias: String,
    val description: String,
    val providerId: String,
    val topLevel: Boolean,
    val builtIn: Boolean,
    val authenticationExecutions: List<AuthenticationExecution>) {
    enum class Requirement {
        ALTERNATIVE, DISABLED, REQUIRED, CONDITIONAL, OPTIONAL
    }

    data class AuthenticationExecution(
        val authenticator: String? = null,
        val requirement: Requirement,
        val priority: Int,
        val userSetupAllowed: Boolean,
        // This looks very weird, but they seem to have this in their codebase, I will assume it means authenticatorFlow
        @Suppress("SpellCheckingInspection")
        val autheticatorFlow: Boolean)
}

data class AuthenticationExecution(
    val id: UUID,
    val requirement: Flow.Requirement,
    val displayName: String,
    val requirementChoices: List<Flow.Requirement>,
    val configurable: Boolean,
    val providerId: String?,
    val level: Int,
    val index: Int,
    val priority: Int,
    val authenticationConfig: String? = null,
    val authenticationFlow: Boolean
)

data class AddFlow(val alias: String,
    val builtIn: Boolean,
    val description: String,
    val providerId: String,
    val topLevel: Boolean)

data class UpdateFlow(
    val id: UUID,
    val alias: String,
    val providerId: String,
    val topLevel: Boolean,
    val description: String,
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "class"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AuthenticationExecutionImport::class, name = "execution"),
    JsonSubTypes.Type(value = SubFlow::class, name = "subFlow")
)
interface AuthenticationFlowAction {}

data class AuthenticationExecutionImport(
    val requirement: Flow.Requirement,
    val providerId: String?,
    val level: Int,
    val index: Int,
    val priority: Int,
    val config: Map<String, String> = emptyMap()) : AuthenticationFlowAction

data class SubFlow(
    val alias: String,
    val description: String,
    val providerId: String,
    val type: String,
    val authenticationExecutions: List<AuthenticationFlowAction>) : AuthenticationFlowAction

data class ImportFlow(
    val alias: String,
    val description: String,
    val providerId: String,
    val topLevel: Boolean,
    val buildIn: Boolean,
    val authenticationExecutions: List<AuthenticationFlowAction>)

data class UpdateFlowInPlace(
    val newAlias: String,
    val description: String,
    val providerId: String,
    val topLevel: Boolean,
    val authenticationExecutions: List<AuthenticationExecutionImport>
)

data class AddFlowExecution(val provider: String?)

data class UpdateFlowExecution(val id: UUID,
    val requirement: Flow.Requirement,
    val level: Int,
    val index: Int,
    val priority: Int,
    val providerId: String?)

data class CopyFlowExecution(val newName: String)

data class AuthenticatorConfig(val alias: String? = null,
    val config: Map<String, String>,
    val id: UUID? = null)
