package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import de.klg71.keycloakmigration.changeControl.actions.AddUserAction
import de.klg71.keycloakmigration.changeControl.actions.UpdateUserAction
import java.util.Objects.isNull

class ParseException(message: String) : RuntimeException(message)

class ActionDeserializer(private val objectMapper: ObjectMapper) : StdDeserializer<Action>(Action::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Action {
        if (isNull(p)) {
            throw ParseException("JsonParser is null")
        }
        return p!!.readObjectNode().run {
            firstField()
        }.let {
            createAction(it)
        }
    }

    private fun ObjectNode.firstField(): Map.Entry<String, JsonNode> {
        if (!fields().hasNext()) {
            throw ParseException("Change is empty!")
        }
        return fields().next()
    }

    private fun JsonParser.readObjectNode() = codec.readTree<ObjectNode>(this)

    private fun createAction(entry: Map.Entry<String, JsonNode>): Action =
            when (entry.key) {
                "addUser" -> {
                    objectMapper.treeToValue<AddUserAction>(entry.value)
                }
                "updateUser" -> {
                    objectMapper.treeToValue<UpdateUserAction>(entry.value)
                }
                else -> throw ParseException("Unkown Change type: ${entry.key}")
            }
}


