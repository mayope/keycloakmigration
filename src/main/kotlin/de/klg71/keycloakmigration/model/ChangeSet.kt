package de.klg71.keycloakmigration.model

import com.fasterxml.jackson.annotation.JsonIgnore
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

    fun alternativeHashes(): List<String> {
        val hashBuilders = mutableListOf<StringBuilder>()
        val initialBuilder = StringBuilder()
        initialBuilder.append(author)
        initialBuilder.append(id)
        realm?.let {
            initialBuilder.append(it)
        }

        hashBuilders.add(initialBuilder)

        return hashBuildersFromChanges(hashBuilders, changes).map { sha256Hex(it.toString()) }
    }

    private fun hashBuildersFromChanges(hashBuilders: MutableList<StringBuilder>, remainingChanges: List<Action>): List<StringBuilder> {
        if (remainingChanges.isEmpty()) {
            return hashBuilders;
        }
        val newBuilders = ArrayList<StringBuilder>();
        for (builder in hashBuilders) {
            val change = remainingChanges.first()
            if (change.alternativeHashes().isNotEmpty()) {
                change.alternativeHashes().forEach {
                    val duplicatedBuilder = StringBuilder(builder.toString())
                    duplicatedBuilder.append(it)
                    newBuilders.add(duplicatedBuilder)
                }
            }
            builder.append(change.hash())
        }
        hashBuilders.addAll(newBuilders)
        return hashBuildersFromChanges(hashBuilders, remainingChanges.drop(1))
    }
}