package de.klg71.keycloakmigration.model

import java.util.*

data class AssignRole(val clientRole: Boolean,
                      val composite: Boolean,
                      val containerId: String,
                      val id: UUID,
                      val name: String)
