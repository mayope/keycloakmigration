package de.klg71.keycloakmigration.keycloakapi.model

import java.util.*


data class Role(val id: UUID,
                val name: String,
                val description: String? = null,
                val composite: Boolean,
                val clientRole: Boolean,
                val containerId: String,
                val attributes: Map<String, List<String>>? = null)
