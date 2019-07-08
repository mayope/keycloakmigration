package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.annotation.JsonIgnore
import de.klg71.keycloakmigration.changeControl.actions.Action

data class ChangeSet(val id: String,
                     val author: String,
                     val changes: List<Action>,
                     @JsonIgnore
                     var path: String="")