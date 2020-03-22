package de.klg71.keycloakmigration.changeControl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.DeleteClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.ImportClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.UpdateClientAction
import de.klg71.keycloakmigration.changeControl.actions.group.AddGroupAction
import de.klg71.keycloakmigration.changeControl.actions.group.AssignRoleToGroupAction
import de.klg71.keycloakmigration.changeControl.actions.group.DeleteGroupAction
import de.klg71.keycloakmigration.changeControl.actions.group.RevokeRoleFromGroupAction
import de.klg71.keycloakmigration.changeControl.actions.group.UpdateGroupAction
import de.klg71.keycloakmigration.changeControl.actions.realm.AddRealmAction
import de.klg71.keycloakmigration.changeControl.actions.realm.DeleteRealmAction
import de.klg71.keycloakmigration.changeControl.actions.role.AddRoleAction
import de.klg71.keycloakmigration.changeControl.actions.role.DeleteRoleAction
import de.klg71.keycloakmigration.changeControl.actions.user.AddUserAction
import de.klg71.keycloakmigration.changeControl.actions.user.AddUserAttributeAction
import de.klg71.keycloakmigration.changeControl.actions.user.AssignGroupAction
import de.klg71.keycloakmigration.changeControl.actions.user.AssignRoleAction
import de.klg71.keycloakmigration.changeControl.actions.user.DeleteUserAction
import de.klg71.keycloakmigration.changeControl.actions.user.DeleteUserAttributeAction
import de.klg71.keycloakmigration.changeControl.actions.user.RevokeGroupAction
import de.klg71.keycloakmigration.changeControl.actions.user.RevokeRoleAction
import de.klg71.keycloakmigration.changeControl.actions.user.UpdateUserAction
import de.klg71.keycloakmigration.changeControl.actions.user.UpdateUserPasswordAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.AddAdLdapAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.DeleteUserFederationAction

class ActionFactory(private val objectMapper: ObjectMapper) {

    internal fun createAction(actionName: String, actionJson: String): Action =
            mapToAction(actionName, actionJson)
                    .apply {
                        yamlNodeValue = actionJson
                    }


    @Suppress("ComplexMethod")
    private fun mapToAction(actionName: String, actionJson: String): Action =
            when (actionName) {
                "addUser" -> objectMapper.readValue<AddUserAction>(actionJson)
                "updateUser" -> objectMapper.readValue<UpdateUserAction>(actionJson)
                "updateUserPassword" -> objectMapper.readValue<UpdateUserPasswordAction>(actionJson)
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
