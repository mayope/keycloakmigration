package de.klg71.keycloakmigration.model


data class AddSimpleClient(val clientId: String,
                           val enabled: Boolean,
                           val attributes: Attributes,
                           val protocol: String,
                           val redirectUris: List<String>)