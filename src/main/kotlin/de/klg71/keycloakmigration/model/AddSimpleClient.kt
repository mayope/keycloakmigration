package de.klg71.keycloakmigration.model


data class AddSimpleClient(val clientId: String,
                           val enabled: Boolean,
                           val attributes: Map<String, String>,
                           val protocol: String,
                           val redirectUris: List<String>)