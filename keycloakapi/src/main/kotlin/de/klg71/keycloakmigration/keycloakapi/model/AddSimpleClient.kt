package de.klg71.keycloakmigration.keycloakapi.model


const val OPENID_CONNECT_PROTOCOL = "openid-connect"

data class AddSimpleClient(val clientId: String,
                           val enabled: Boolean,
                           val attributes: Map<String, String>,
                           val protocol: String = OPENID_CONNECT_PROTOCOL,
                           val redirectUris: List<String>,
                           val secret: String? = null,
                           val authorizationServicesEnabled: Boolean,
                           var serviceAccountsEnabled: Boolean,
                           val publicClient: Boolean)
