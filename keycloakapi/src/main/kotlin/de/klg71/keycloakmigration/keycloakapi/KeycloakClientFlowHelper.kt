@file:Suppress("TooManyFunctions")

package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.AddFlow
import de.klg71.keycloakmigration.keycloakapi.model.AddFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.AddSubFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationFlowAction
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import de.klg71.keycloakmigration.keycloakapi.model.CopyFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.ImportFlow
import de.klg71.keycloakmigration.keycloakapi.model.SubFlow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowInPlace
import java.util.*

fun KeycloakClient.importFlow(realm: String, importFlow: ImportFlow): UUID {
    if (flows(realm).any { it.alias == importFlow.alias }) {
        throw KeycloakApiException("Import Flow failed, Flow: ${importFlow.alias} already exists")
    }
    return createFlow(realm, importFlow).also {
        addExecutions(realm, importFlow.alias, importFlow.authenticationExecutions)
    }
}

private fun KeycloakClient.configureSubFlow(
    realm: String,
    flowAlias: String,
    subflow: AddSubFlow,
    executions: List<AuthenticationFlowAction>
): UUID {
    return addSubFlow(
        realm,
        flowAlias,
        subflow
    ).extractLocationUUID().also {
        addExecutions(realm, subflow.alias, executions)
    }
}

/**
 * Adds a list of [executions] into the flow (or subflow) identified by [flowAlias]. Plain executions
 * are added directly; nested subflows are configured recursively. This is the single place that maps
 * an [AuthenticationFlowAction] to the call that materializes it.
 */
fun KeycloakClient.addExecutions(
    realm: String,
    flowAlias: String,
    executions: List<AuthenticationFlowAction>
) {
    executions.forEach {
        when (it) {
            is AuthenticationExecutionImport -> addExecution(realm, flowAlias, it)
            // The create endpoint reads "provider", so map SubFlow.providerId onto AddSubFlow.provider.
            is SubFlow -> configureSubFlow(
                realm, flowAlias,
                AddSubFlow(it.alias, it.description, it.type, it.providerId),
                it.authenticationExecutions
            )
        }
    }
}

fun KeycloakClient.copyAuthFlow(realm: String, flowAlias: String, newName: String) {
    val flows = flows(realm)

    if (flows.none { it.alias == flowAlias }) {
        throw KeycloakApiException("Copy Flow failed, Flow: $flowAlias does not exist")
    }

    if (flows.any { it.alias == newName }) {
        throw KeycloakApiException("Copy Flow failed, Flow: $newName already exists")
    }

    copyFlow(realm, flowAlias, CopyFlowExecution(newName))
}

fun KeycloakClient.updateFlowInPlace(realm: String, alias: String, updateFlow: UpdateFlowInPlace) {
    if (flows(realm).none { it.alias == alias }) {
        throw KeycloakApiException("Update Flow failed, Flow: $alias does not exist")
    }
    val flow = flows(realm).first { it.alias == alias }

    updateFlow(
        realm, flow.id,
        UpdateFlow(flow.id, updateFlow.newAlias, updateFlow.providerId, updateFlow.topLevel, updateFlow.description)
    )
    updateAuthExecutors(updateFlow.newAlias, realm, updateFlow.authenticationExecutions)
}

private fun KeycloakClient.createFlow(realm: String, importFlow: ImportFlow): UUID {
    return addFlow(
        realm, AddFlow(
            importFlow.alias, importFlow.buildIn, importFlow.description, importFlow.providerId, importFlow.topLevel
        )
    ).extractLocationUUID()
}

private fun KeycloakClient.addExecution(
    realm: String,
    flowAlias: String,
    executionImport: AuthenticationExecutionImport
) {
    val executionId =
        addFlowExecution(realm, flowAlias, AddFlowExecution(executionImport.providerId)).extractLocationUUID()
    updateFlowExecution(
        realm, flowAlias, UpdateFlowExecution(
            executionId, executionImport.requirement, executionImport.level, executionImport.index,
            executionImport.priority, executionImport.providerId
        )
    )
    updateFlowExecutionWithNewConfiguration(
        realm, executionId.toString(), AuthenticatorConfig(
            executionImport.providerId + "_config",
            executionImport.config,
            null
        )
    )
}

private fun KeycloakClient.updateAuthExecutors(
    flowAlias: String,
    realm: String,
    executors: List<AuthenticationExecutionImport>
) {
    flowExecutions(realm, flowAlias).forEach {
        deleteFlowExecution(realm, it.id)
    }
    executors.forEach {
        addExecution(realm, flowAlias, it)
    }
}

fun KeycloakClient.executionsToImport(realm: String, flowAlias: String) = flowExecutions(realm, flowAlias).map {
    AuthenticationExecutionImport(
        it.id, it.requirement, it.providerId, it.level, it.index, it.priority,
        if (it.authenticationConfig == null) mapOf() else getAuthenticatorConfiguration(
            realm,
            it.authenticationConfig
        ).config
    )
}
