package de.klg71.keycloakmigration.changeControl.model

data class ChangeSetEntry(val path: String, val relativeToFile: Boolean = true)

data class ChangeLog(val includes: List<ChangeSetEntry>)
