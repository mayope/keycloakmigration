package de.klg71.keycloakmigration.keycloakapi.model

/**
 * A place in a realm that uses a top-level authentication flow.
 */
sealed interface FlowBinding

/** A realm flow setting, such as `browserFlow`. Holds the flow's alias. */
data class RealmFlowBinding(val field: String) : FlowBinding

/** A client's `authenticationFlowBindingOverrides` entry, such as `browser`. Holds the flow's id. */
data class ClientFlowBinding(val clientUuid: String, val clientId: String, val override: String) : FlowBinding

/** An identity provider flow setting, such as `firstBrokerLoginFlowAlias`. Holds the flow's alias. */
data class IdentityProviderFlowBinding(val alias: String, val field: String) : FlowBinding
