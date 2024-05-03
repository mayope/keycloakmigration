package de.klg71.keycloakmigration.keycloakapi.model

data class RequiredActionProviderItem(
        val alias: String,
        val config: Map<String, String>?,
        val defaultAction: Boolean,
        val enabled: Boolean,
        val name: String,
        val priority: Int?,
        val providerId: String)
