package de.klg71.keycloakmigration.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Realm(val id: String,
                 val realm: String,
                 val displayName: String)
