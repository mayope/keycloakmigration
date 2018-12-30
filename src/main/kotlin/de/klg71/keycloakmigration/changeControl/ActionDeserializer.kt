package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.AddAdLdapAction
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.DeleteClientAction
import de.klg71.keycloakmigration.changeControl.actions.group.AddGroupAction
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.changeControl.actions.role.DeleteRoleAction
import de.klg71.keycloakmigration.changeControl.actions.user.*
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
                "addUser" -> objectMapper.treeToValue<AddUserAction>(entry.value)
                "updateUser" -> objectMapper.treeToValue<UpdateUserAction>(entry.value)
                "deleteUser" -> objectMapper.treeToValue<DeleteUserAction>(entry.value)
                "assignRole" -> objectMapper.treeToValue<AssignRoleAction>(entry.value)
                "revokeRole" -> objectMapper.treeToValue<RevokeRoleAction>(entry.value)
                "addUserAttribute" -> objectMapper.treeToValue<AddUserAttributeAction>(entry.value)
                "deleteUserAttribute" -> objectMapper.treeToValue<DeleteUserAttributeAction>(entry.value)
                "addRole" -> objectMapper.treeToValue<AddRoleAction>(entry.value)
                "deleteRole" -> objectMapper.treeToValue<DeleteRoleAction>(entry.value)
                "addSimpleClient" -> objectMapper.treeToValue<AddSimpleClientAction>(entry.value)
                "deleteClient" -> objectMapper.treeToValue<DeleteClientAction>(entry.value)
                "addGroup" -> objectMapper.treeToValue<AddGroupAction>(entry.value)
                "addAdLdap" -> objectMapper.treeToValue<AddAdLdapAction>(entry.value)
                else -> throw ParseException("Unkown Change type: ${entry.key}")
            }
}


