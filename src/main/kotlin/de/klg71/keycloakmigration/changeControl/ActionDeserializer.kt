package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.actions.Action
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
import de.klg71.keycloakmigration.changeControl.actions.userfederation.AddAdLdapAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.DeleteUserFederationAction
import org.apache.commons.text.StringSubstitutor
import java.util.Objects.isNull

class ParseException(message: String, cause: Exception?) : RuntimeException(message, cause) {
    constructor(message: String) : this(message, null)
}

class ActionDeserializer(private val objectMapper: ObjectMapper) : StdDeserializer<Action>(Action::class.java) {
    private val systemEnvSubstitutor = StringSubstitutor(System.getenv())

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
                systemEnvSubstitutor.replace(it)
            }.let {
                mapToAction(entry.key, it)
            }.apply {
                yamlNodeValue = entry.value.toString()
            }

    private fun mapToAction(actionName: String, actionJson: String): Action =
            when (actionName) {
                "addUser" -> objectMapper.readValue<AddUserAction>(actionJson)
                "updateUser" -> objectMapper.readValue<UpdateUserAction>(actionJson)
                "deleteUser" -> objectMapper.readValue<DeleteUserAction>(actionJson)
                "addUserAttribute" -> objectMapper.readValue<AddUserAttributeAction>(actionJson)
                "deleteUserAttribute" -> objectMapper.readValue<DeleteUserAttributeAction>(actionJson)
                "assignGroup" -> objectMapper.readValue<AssignGroupAction>(actionJson)
                "revokeGroup" -> objectMapper.readValue<RevokeGroupAction>(actionJson)

                "assignRole" -> objectMapper.readValue<AssignRoleAction>(actionJson)
                "revokeRole" -> objectMapper.readValue<RevokeRoleAction>(actionJson)

                "addRole" -> objectMapper.readValue<AddRoleAction>(actionJson)
                "deleteRole" -> objectMapper.readValue<DeleteRoleAction>(actionJson)

                "addSimpleClient" -> objectMapper.readValue<AddSimpleClientAction>(actionJson)
                "importClient" -> objectMapper.readValue<ImportClientAction>(actionJson)
                "updateClient" -> objectMapper.readValue<UpdateClientAction>(actionJson)
                "deleteClient" -> objectMapper.readValue<DeleteClientAction>(actionJson)

                "addGroup" -> objectMapper.readValue<AddGroupAction>(actionJson)
                "deleteGroup" -> objectMapper.readValue<DeleteGroupAction>(actionJson)
                "assignRoleToGroup" -> objectMapper.readValue<AssignRoleToGroupAction>(actionJson)
                "revokeRoleFromGroup" -> objectMapper.readValue<RevokeRoleFromGroupAction>(actionJson)

                "addAdLdap" -> objectMapper.readValue<AddAdLdapAction>(actionJson)
                "deleteUserFederation" -> objectMapper.readValue<DeleteUserFederationAction>(actionJson)

                "addRealm" -> objectMapper.readValue<AddRealmAction>(actionJson)
                "deleteRealm" -> objectMapper.readValue<DeleteRealmAction>(actionJson)
                "updateGroup" -> objectMapper.readValue<UpdateGroupAction>(actionJson)

                else -> throw ParseException("Unknown Change type: $actionName")
            }
}


