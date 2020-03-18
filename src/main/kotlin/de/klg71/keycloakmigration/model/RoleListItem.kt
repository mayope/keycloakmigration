package de.klg71.keycloakmigration.model

import java.util.*


data class RoleListItem(val id: UUID,
                        val name: String,
                        val description: String?,
                        val composite: Boolean,
                        val clientRole: Boolean,
                        val containerId: String)
