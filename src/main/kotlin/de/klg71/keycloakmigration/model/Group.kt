package de.klg71.keycloakmigration.model

import java.util.*

data class Group(val id: UUID,
                 val name: String,
                 val path: String,
                 val attributes: Map<String, List<String>>,
                 val realmRoles: List<String>,
                 val clientRoles: Map<String, List<String>>,
                 val subGroups: List<Group>,
                 val access: Map<String, Boolean>?)
