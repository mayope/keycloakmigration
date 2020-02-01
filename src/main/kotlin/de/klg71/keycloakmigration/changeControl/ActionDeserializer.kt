package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.AddAdLdapAction
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.DeleteClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.ImportClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.UpdateClientAction
import de.klg71.keycloakmigration.changeControl.actions.group.*
import de.klg71.keycloakmigration.changeControl.actions.realm.AddRealmAction
import de.klg71.keycloakmigration.changeControl.actions.realm.DeleteRealmAction
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.changeControl.actions.role.DeleteRoleAction
import de.klg71.keycloakmigration.changeControl.actions.user.*
import org.apache.commons.text.StringEscapeUtils
import org.apache.commons.text.StringSubstitutor
import java.util.Objects.isNull

class ParseException(message: String, cause:Exception?) : RuntimeException(message, cause){
    constructor(message:String):this(message,null)
}

class ActionDeserializer(private val objectMapper: ObjectMapper) : StdDeserializer<Action>(Action::class.java) {
    private val stringSubstitutor = StringSubstitutor(System.getenv())
    private val escaper = StringEscapeUtils.ESCAPE_JSON;

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
            entry.value.toString().let {
                stringSubstitutor.replace(it)
            }.let {
            when (entry.key) {
                "addUser" -> objectMapper.readValue<AddUserAction>(it)
                "updateUser" -> objectMapper.readValue<UpdateUserAction>(it)
                "deleteUser" -> objectMapper.readValue<DeleteUserAction>(it)
                "addUserAttribute" -> objectMapper.readValue<AddUserAttributeAction>(it)
                "deleteUserAttribute" -> objectMapper.readValue<DeleteUserAttributeAction>(it)
                "assignGroup" -> objectMapper.readValue<AssignGroupAction>(it)
                "revokeGroup" -> objectMapper.readValue<RevokeGroupAction>(it)

                "assignRole" -> objectMapper.readValue<AssignRoleAction>(it)
                "revokeRole" -> objectMapper.readValue<RevokeRoleAction>(it)

                "addRole" -> objectMapper.readValue<AddRoleAction>(it)
                "deleteRole" -> objectMapper.readValue<DeleteRoleAction>(it)

                "addSimpleClient" -> objectMapper.readValue<AddSimpleClientAction>(it)
                "importClient" -> objectMapper.readValue<ImportClientAction>(it)
                "updateClient" -> objectMapper.readValue<UpdateClientAction>(it)
                "deleteClient" -> objectMapper.readValue<DeleteClientAction>(it)

                "addGroup" -> objectMapper.readValue<AddGroupAction>(it)
                "deleteGroup" -> objectMapper.readValue<DeleteGroupAction>(it)
                "assignRoleToGroup" -> objectMapper.readValue<AssignRoleToGroupAction>(it)
                "revokeRoleFromGroup" -> objectMapper.readValue<RevokeRoleFromGroupAction>(it)

                "addAdLdap" -> objectMapper.readValue<AddAdLdapAction>(it)

                "addRealm" -> objectMapper.readValue<AddRealmAction>(it)
                "deleteRealm" -> objectMapper.readValue<DeleteRealmAction>(it)
                "updateGroup" -> objectMapper.readValue<UpdateGroupAction>(it)

                else -> throw ParseException("Unkown Change type: ${entry.key}")
            }
            }.apply {
                yamlNodeValue = entry.value.toString()
            }
}


