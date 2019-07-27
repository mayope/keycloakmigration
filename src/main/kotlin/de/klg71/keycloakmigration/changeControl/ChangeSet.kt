package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.annotation.JsonIgnore
import de.klg71.keycloakmigration.changeControl.actions.Action
import org.apache.commons.codec.digest.DigestUtils.sha256Hex

data class ChangeSet(val id: String,
                     val author: String,
                     val changes: List<Action>,
                     @JsonIgnore
                     var path: String = "") {

    fun hash() = StringBuilder().let { builder ->
        builder.append(author)
        builder.append(id)
        changes.forEach {
            it.path = path
            builder.append(it.hash())
        }
        builder.toString()
    }.let {
        sha256Hex(it)
    }!!
}