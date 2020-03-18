package de.klg71.keycloakmigration.model


data class UpdateGroup(val name: String,
                       val path: String,
                       val attributes: Map<String, List<String>>,
                       val access: Map<String, Boolean>,
                       val clientRoles: Map<String, List<String>>,
                       val realmRoles: List<String>,
                       val subGroups: List<Group>)
