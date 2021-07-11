package de.klg71.keycloakmigration.changeControl.actions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.actions.client.AddRoleScopeMappingAction
import de.klg71.keycloakmigration.changeControl.actions.client.AddSimpleClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.AssignRoleToClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.DeleteClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.DeleteRoleScopeMappingAction
import de.klg71.keycloakmigration.changeControl.actions.client.ImportClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.UpdateClientAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddAudienceMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddClientAudienceMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddClientGroupMembershipMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddClientMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddClientUserAttributeMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddClientUserRealmRoleMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddGroupMembershipMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddUserAttributeMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.AddUserRealmRoleMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.DeleteClientMapperAction
import de.klg71.keycloakmigration.changeControl.actions.client.mapper.DeleteMapperAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.AddClientScopeAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.AssignDefaultClientScopeAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeAudienceMapperAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeGroupMembershipMapperAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeMapperAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeUserAttributeMapperAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.AddClientScopeUserRealmRoleMapperAction
import de.klg71.keycloakmigration.changeControl.actions.clientscope.mapper.DeleteClientScopeMapperAction
import de.klg71.keycloakmigration.changeControl.actions.flow.AddFlowAction
import de.klg71.keycloakmigration.changeControl.actions.flow.DeleteFlowAction
import de.klg71.keycloakmigration.changeControl.actions.flow.UpdateFlowAction
import de.klg71.keycloakmigration.changeControl.actions.group.AddGroupAction
import de.klg71.keycloakmigration.changeControl.actions.group.AssignRoleToGroupAction
import de.klg71.keycloakmigration.changeControl.actions.group.DeleteGroupAction
import de.klg71.keycloakmigration.changeControl.actions.group.RevokeRoleFromGroupAction
import de.klg71.keycloakmigration.changeControl.actions.group.UpdateGroupAction
import de.klg71.keycloakmigration.changeControl.actions.identityprovider.AddIdentityProviderAction
import de.klg71.keycloakmigration.changeControl.actions.identityprovider.AddKeycloakIdentityProviderAction
import de.klg71.keycloakmigration.changeControl.actions.identityprovider.DeleteIdentityProviderAction
import de.klg71.keycloakmigration.changeControl.actions.realm.AddRealmAction
import de.klg71.keycloakmigration.changeControl.actions.realm.DeleteRealmAction
import de.klg71.keycloakmigration.changeControl.actions.realm.UpdateRealmAction
import de.klg71.keycloakmigration.changeControl.actions.requiredactions.AddRequiredActionAction
import de.klg71.keycloakmigration.changeControl.actions.requiredactions.DeleteRequiredActionAction
import de.klg71.keycloakmigration.changeControl.actions.requiredactions.UpdateRequiredActionAction
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
import de.klg71.keycloakmigration.changeControl.actions.userfederation.AddUserFederationAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.DeleteUserFederationAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper.AddAdLdapFullNameMapperAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper.AddAdLdapGroupMapperAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper.AddAdLdapHardcodedRoleMapperAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper.AddAdLdapMapperAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper.AddAdLdapUserAccountControlMapperAction
import de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper.AddAdLdapUserAttributeMapperAction

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
            "assignRoleToClient" -> objectMapper.readValue<AssignRoleToClientAction>(actionJson)
            "addRoleScopeMapping" -> objectMapper.readValue<AddRoleScopeMappingAction>(actionJson)
            "deleteRoleScopeMapping" -> objectMapper.readValue<DeleteRoleScopeMappingAction>(actionJson)

            "addClientScope" -> objectMapper.readValue<AddClientScopeAction>(actionJson)
            "assignDefaultClientScope" -> objectMapper.readValue<AssignDefaultClientScopeAction>(actionJson)

            "addGroup" -> objectMapper.readValue<AddGroupAction>(actionJson)
            "deleteGroup" -> objectMapper.readValue<DeleteGroupAction>(actionJson)
            "updateGroup" -> objectMapper.readValue<UpdateGroupAction>(actionJson)
            "assignRoleToGroup" -> objectMapper.readValue<AssignRoleToGroupAction>(actionJson)
            "revokeRoleFromGroup" -> objectMapper.readValue<RevokeRoleFromGroupAction>(actionJson)

            "addMapper" -> objectMapper.readValue<AddMapperAction>(actionJson)
            "deleteMapper" -> objectMapper.readValue<DeleteMapperAction>(actionJson)
            "addAudienceMapper" -> objectMapper.readValue<AddAudienceMapperAction>(actionJson)
            "addGroupMembershipMapper" -> objectMapper.readValue<AddGroupMembershipMapperAction>(actionJson)
            "addUserAttributeMapper" -> objectMapper.readValue<AddUserAttributeMapperAction>(actionJson)
            "addUserRealmRoleMapper" -> objectMapper.readValue<AddUserRealmRoleMapperAction>(actionJson)

            "addClientMapper" -> objectMapper.readValue<AddClientMapperAction>(actionJson)
            "deleteClientMapper" -> objectMapper.readValue<DeleteClientMapperAction>(actionJson)
            "addClientAudienceMapper" -> objectMapper.readValue<AddClientAudienceMapperAction>(actionJson)
            "addClientGroupMembershipMapper" -> objectMapper.readValue<AddClientGroupMembershipMapperAction>(actionJson)
            "addClientUserAttributeMapper" -> objectMapper.readValue<AddClientUserAttributeMapperAction>(actionJson)
            "addClientUserRealmRoleMapper" -> objectMapper.readValue<AddClientUserRealmRoleMapperAction>(actionJson)

            "addClientScopeMapper" -> objectMapper.readValue<AddClientScopeMapperAction>(actionJson)
            "deleteClientScopeMapper" -> objectMapper.readValue<DeleteClientScopeMapperAction>(actionJson)
            "addClientScopeAudienceMapper" -> objectMapper.readValue<AddClientScopeAudienceMapperAction>(actionJson)
            "addClientScopeGroupMembershipMapper" -> objectMapper.readValue<AddClientScopeGroupMembershipMapperAction>(
                actionJson
            )
            "addClientScopeUserAttributeMapper" -> objectMapper.readValue<AddClientScopeUserAttributeMapperAction>(
                actionJson
            )
            "addClientScopeUserRealmRoleMapper" -> objectMapper.readValue<AddClientScopeUserRealmRoleMapperAction>(
                actionJson
            )

            "addAdLdap" -> objectMapper.readValue<AddAdLdapAction>(actionJson)
            "addUserFederation" -> objectMapper.readValue<AddUserFederationAction>(actionJson)
            "deleteUserFederation" -> objectMapper.readValue<DeleteUserFederationAction>(actionJson)
            "addAdLdapFullNameMapper" -> objectMapper.readValue<AddAdLdapFullNameMapperAction>(actionJson)
            "addAdLdapGroupMapper" -> objectMapper.readValue<AddAdLdapGroupMapperAction>(actionJson)
            "addAdLdapHardcodedRoleMapper" -> objectMapper.readValue<AddAdLdapHardcodedRoleMapperAction>(actionJson)
            "addAdLdapMapper" -> objectMapper.readValue<AddAdLdapMapperAction>(actionJson)
            "addAdLdapUserAccountControlMapper" -> objectMapper.readValue<AddAdLdapUserAccountControlMapperAction>(
                actionJson
            )
            "addAdLdapUserAttributeMapper" -> objectMapper.readValue<AddAdLdapUserAttributeMapperAction>(actionJson)

            "addRealm" -> objectMapper.readValue<AddRealmAction>(actionJson)
            "deleteRealm" -> objectMapper.readValue<DeleteRealmAction>(actionJson)
            "updateRealm" -> objectMapper.readValue<UpdateRealmAction>(actionJson)

            "addIdentityProvider" -> objectMapper.readValue<AddIdentityProviderAction>(actionJson)
            "addKeycloakIdentityProvider" -> objectMapper.readValue<AddKeycloakIdentityProviderAction>(actionJson)
            "deleteIdentityProvider" -> objectMapper.readValue<DeleteIdentityProviderAction>(actionJson)

            "addFlow" -> objectMapper.readValue<AddFlowAction>(actionJson)
            "deleteFlow" -> objectMapper.readValue<DeleteFlowAction>(actionJson)
            "updateFlow" -> objectMapper.readValue<UpdateFlowAction>(actionJson)

            "addRequiredAction" -> objectMapper.readValue<AddRequiredActionAction>(actionJson)
            "deleteRequiredAction" -> objectMapper.readValue<DeleteRequiredActionAction>(actionJson)
            "updateRequiredAction" -> objectMapper.readValue<UpdateRequiredActionAction>(actionJson)

            else -> throw ParseException(
                "Unknown Change type: $actionName"
            )
        }
}
