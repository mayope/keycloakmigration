package de.klg71.keycloakmigration.keycloakapi

class KeycloakApiException(message: String, statusCode: Int? = null) : RuntimeException(message + (statusCode?.let {
    "Status: $it"
} ?: ""))
