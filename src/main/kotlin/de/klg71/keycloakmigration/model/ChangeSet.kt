package de.klg71.keycloakmigration.model

import com.fasterxml.jackson.annotation.JsonIgnore
import de.klg71.keycloakmigration.changeControl.ParseException
import de.klg71.keycloakmigration.changeControl.actions.Action
import org.apache.commons.codec.digest.DigestUtils.sha256Hex

data class ChangeSet(val id: String,
                     val author: String,
                     val changes: List<Action>,
                     val realm: String? = null,
                     @JsonIgnore
                     var path: String = "") {

    init {
        changes.forEach {
            it.path = path
            if (it.realm == null) {
                if (realm != null) {
                    it.realm = realm
                }
            }
        }
    }

    fun hash() = StringBuilder().let { builder ->
        builder.append(author)
        builder.append(id)
        realm?.let {
            builder.append(it)
        }
        changes.forEach {
            builder.append(it.hash())
        }
        builder.toString()
    }.let {
        sha256Hex(it)
    }!!
}