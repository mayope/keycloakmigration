package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.ClientFlowBinding
import de.klg71.keycloakmigration.keycloakapi.model.FlowBinding
import de.klg71.keycloakmigration.keycloakapi.model.IdentityProviderFlowBinding
import de.klg71.keycloakmigration.keycloakapi.model.RealmFlowBinding
import feign.Response
import java.util.UUID

/** The realm settings that name a top-level flow by alias. */
val REALM_FLOW_FIELDS = listOf(
    "browserFlow", "registrationFlow", "directGrantFlow", "resetCredentialsFlow", "clientAuthenticationFlow",
    "dockerAuthenticationFlow", "firstBrokerLoginFlow"
)

/** The identity provider settings that name a top-level flow by alias. */
val IDENTITY_PROVIDER_FLOW_FIELDS = listOf("firstBrokerLoginFlowAlias", "postBrokerLoginFlowAlias")

/**
 * Lists everything in the realm that uses the top-level flow [flowAlias] (with id [flowId]): realm flow
 * settings, client flow overrides and identity provider flows.
 */
fun KeycloakClient.flowBindings(realm: String, flowAlias: String, flowId: UUID): List<FlowBinding> {
    val realmRepresentation = realmRepresentation(realm)
    val realmBindings = REALM_FLOW_FIELDS
        .filter { realmRepresentation[it] == flowAlias }
        .map { RealmFlowBinding(it) }

    val clientBindings = clientRepresentations(realm).flatMap { client ->
        val overrides = client["authenticationFlowBindingOverrides"] as? Map<*, *> ?: emptyMap<Any, Any>()
        overrides.filterValues { it == flowId.toString() }.keys.map {
            ClientFlowBinding(client["id"].toString(), client["clientId"].toString(), it.toString())
        }
    }

    val identityProviderBindings = allIdentityProviderRepresentations(realm).flatMap { identityProvider ->
        IDENTITY_PROVIDER_FLOW_FIELDS
            .filter { identityProvider[it] == flowAlias }
            .map { IdentityProviderFlowBinding(identityProvider["alias"].toString(), it) }
    }

    return realmBindings + clientBindings + identityProviderBindings
}

private const val IDENTITY_PROVIDER_PAGE_SIZE = 100

/** Every identity provider of the realm, read a page at a time. */
fun KeycloakClient.allIdentityProviderRepresentations(realm: String): List<Map<String, Any?>> =
    generateSequence(0) { it + IDENTITY_PROVIDER_PAGE_SIZE }
        .map { identityProviderRepresentations(realm, it, IDENTITY_PROVIDER_PAGE_SIZE) }
        .takeWhile { it.isNotEmpty() }
        .flatten()
        .toList()

/**
 * Points [binding] at the top-level flow [flowAlias] (with id [flowId]). Each binding is one update, so a
 * login sees either the flow before or the flow after.
 */
fun KeycloakClient.moveFlowBinding(realm: String, binding: FlowBinding, flowAlias: String, flowId: UUID) {
    when (binding) {
        is RealmFlowBinding ->
            updateRealmRepresentation(realm, mapOf(binding.field to flowAlias))
                .checkBindingUpdate("realm $realm ${binding.field}")

        is ClientFlowBinding -> {
            val client = clientRepresentation(realm, binding.clientUuid).toMutableMap()
            val overrides = (client["authenticationFlowBindingOverrides"] as? Map<*, *>).orEmpty()
                .entries.associate { it.key.toString() to it.value }
                .plus(binding.override to flowId.toString())
            client["authenticationFlowBindingOverrides"] = overrides
            updateClientRepresentation(realm, binding.clientUuid, client)
                .checkBindingUpdate("client ${binding.clientId} ${binding.override} override")
        }

        is IdentityProviderFlowBinding -> {
            val identityProvider = identityProviderRepresentation(realm, binding.alias).toMutableMap()
            identityProvider[binding.field] = flowAlias
            updateIdentityProviderRepresentation(realm, binding.alias, identityProvider)
                .checkBindingUpdate("identity provider ${binding.alias} ${binding.field}")
        }
    }
}

private fun Response.checkBindingUpdate(what: String) {
    if (!isSuccessful()) {
        throw KeycloakApiException(
            "Failed to update the flow of $what: ${body()?.asReader()?.readText()}", statusCode = status()
        )
    }
}
