package de.klg71.keycloakmigration.keycloakapi.model


data class AddUser(val username: String,
                   val enabled: Boolean = true,
                   val emailVerified: Boolean = true,
                   val attributes: Map<String, List<String>> = emptyMap())
