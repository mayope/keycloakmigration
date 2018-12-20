package de.klg71.keycloakmigration.model

data class ChangeSetEntry(val path: String)

data class ChangeLog(val includes: List<ChangeSetEntry>)