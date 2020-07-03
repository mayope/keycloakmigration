package de.klg71.keycloakmigration.keycloakapi.model

import java.util.*

data class AssignGroup(val realm: String,
                       val groupId: UUID,
                       val userId: UUID)
