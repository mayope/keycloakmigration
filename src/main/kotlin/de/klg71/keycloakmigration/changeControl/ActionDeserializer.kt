package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import de.klg71.keycloakmigration.changeControl.actions.Action
import java.util.Objects.isNull

class ParseException(message: String, cause: Exception?) : RuntimeException(message, cause) {
    constructor(message: String) : this(message, null)
}

/**
 * Deserializes Actions from ChangeLog files.
 * Replaces ${} placeholders with system environment variables and variables passed to it at start.
 * If the same identifier is given as environment variable and parameter the environment variable wins.
 */
class ActionDeserializer(private val actionFactory: ActionFactory) : StdDeserializer<Action>(Action::class.java) {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Action {
        if (isNull(p)) {
            throw ParseException("JsonParser is null")
        }
        return p!!.readObjectNode().run {
            firstField()
        }.let {
            actionFactory.createAction(it.key, it.value.toString())
        }
    }

    private fun ObjectNode.firstField(): Map.Entry<String, JsonNode> {
        if (!fields().hasNext()) {
            throw ParseException("Change is empty!")
        }
        return fields().next()
    }

    private fun JsonParser.readObjectNode() = codec.readTree<ObjectNode>(this)
}


