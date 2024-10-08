package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.AddClient
import de.klg71.keycloakmigration.keycloakapi.model.AddClientScope
import de.klg71.keycloakmigration.keycloakapi.model.AddFlow
import de.klg71.keycloakmigration.keycloakapi.model.AddFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.AddGroup
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProvider
import de.klg71.keycloakmigration.keycloakapi.model.AddIdentityProviderMapper
import de.klg71.keycloakmigration.keycloakapi.model.AddMapper
import de.klg71.keycloakmigration.keycloakapi.model.AddOrganization
import de.klg71.keycloakmigration.keycloakapi.model.AddRealm
import de.klg71.keycloakmigration.keycloakapi.model.AddRole
import de.klg71.keycloakmigration.keycloakapi.model.AddSimpleClient
import de.klg71.keycloakmigration.keycloakapi.model.AddUser
import de.klg71.keycloakmigration.keycloakapi.model.AddUserFederation
import de.klg71.keycloakmigration.keycloakapi.model.AddUserFederationMapper
import de.klg71.keycloakmigration.keycloakapi.model.AssignClientScope
import de.klg71.keycloakmigration.keycloakapi.model.AssignGroup
import de.klg71.keycloakmigration.keycloakapi.model.AssignRole
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecution
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticatorConfig
import de.klg71.keycloakmigration.keycloakapi.model.Client
import de.klg71.keycloakmigration.keycloakapi.model.ClientListItem
import de.klg71.keycloakmigration.keycloakapi.model.ClientScope
import de.klg71.keycloakmigration.keycloakapi.model.ClientScopeItem
import de.klg71.keycloakmigration.keycloakapi.model.ClientSecret
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.Group
import de.klg71.keycloakmigration.keycloakapi.model.GroupListItem
import de.klg71.keycloakmigration.keycloakapi.model.IdentityProvider
import de.klg71.keycloakmigration.keycloakapi.model.IdentityProviderMapper
import de.klg71.keycloakmigration.keycloakapi.model.ImportClientRepresentation
import de.klg71.keycloakmigration.keycloakapi.model.Mapper
import de.klg71.keycloakmigration.keycloakapi.model.Organization
import de.klg71.keycloakmigration.keycloakapi.model.Realm
import de.klg71.keycloakmigration.keycloakapi.model.RealmProfile
import de.klg71.keycloakmigration.keycloakapi.model.RequiredActionProviderItem
import de.klg71.keycloakmigration.keycloakapi.model.ResetPassword
import de.klg71.keycloakmigration.keycloakapi.model.Role
import de.klg71.keycloakmigration.keycloakapi.model.RoleListItem
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlow
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlowExecution
import de.klg71.keycloakmigration.keycloakapi.model.UpdateGroup
import de.klg71.keycloakmigration.keycloakapi.model.UpdateIdentityProvider
import de.klg71.keycloakmigration.keycloakapi.model.User
import de.klg71.keycloakmigration.keycloakapi.model.UserFederation
import de.klg71.keycloakmigration.keycloakapi.model.UserFederationMapper
import de.klg71.keycloakmigration.keycloakapi.model.UserGroupListItem
import feign.Headers
import feign.Param
import feign.RequestLine
import feign.Response
import java.util.UUID

data class RealmName(val realm: String)

/**
 * Interface access resources on keycloak. Build with [initKeycloakClient]
 */
@Suppress("TooManyFunctions")
interface KeycloakClient {
    @RequestLine("GET /admin/realms")
    fun realms(): List<Realm>

    @RequestLine("GET /admin/realms/{realm}/users/profile")
    fun realmUserProfile(@Param("realm") realm: String): RealmProfile

    @RequestLine("GET /admin/realms")
    fun realmNames(): List<RealmName>

    @RequestLine("GET /admin/realms/{realm}/users?max={max}")
    fun users(@Param("realm") realm: String, @Param("max") max: Int = 100): List<User>

    @RequestLine("GET /admin/realms/{realm}/users?username={username}&max={max}&exact={exact}&from={from}")
    fun searchByUsername(@Param("username") username: String,
        @Param("realm") realm: String,
        @Param("exact") exact: Boolean = false,
        @Param("from") from: Int = 0,
        @Param("max") max: Int = 100): List<User>

    @RequestLine("GET /admin/realms/{realm}/users?search={search}&max={max}")
    fun searchUser(@Param("search") search: String,
        @Param("realm") realm: String,
        @Param("max") max: Int = 100): List<User>
    @RequestLine("GET /admin/realms/{realm}/users?q={query}&max={max}&from={from}")
    fun searchUserByQuery(@Param("query") query: String,
        @Param("realm") realm: String,
        @Param("from") from: Int = 0,
        @Param("max") max: Int = 100): List<User>


    @RequestLine("DELETE /admin/realms/{realm}/users/{user-id}")
    fun deleteUser(@Param("user-id") userId: UUID, @Param("realm") realm: String)

    @RequestLine("POST /admin/realms/{realm}/users")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addUser(user: AddUser, @Param("realm") realm: String): Response

    @RequestLine("GET /admin/realms/{realm}/users/{user-id}")
    fun user(@Param("user-id") userId: UUID, @Param("realm") realm: String): User

    @RequestLine("GET /admin/realms/{realm}/users/{user-id}")
    fun tryUser(@Param("user-id") userId: UUID, @Param("realm") realm: String): Response

    @RequestLine("PUT /admin/realms/{realm}/users/{user-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateUser(@Param("user-id") userId: UUID, user: User, @Param("realm") realm: String)

    @RequestLine("PUT /admin/realms/{realm}/users/{user-id}/reset-password")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateUserPassword(@Param("user-id") userId: UUID, resetPassword: ResetPassword, @Param("realm") realm: String)

    @RequestLine("POST /admin/realms/{realm}/users/{user-id}/role-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignRealmRoles(roles: List<AssignRole>, @Param("realm") realm: String, @Param("user-id") userId: UUID)

    @RequestLine("PUT /admin/realms/{realm}/users/{user-id}/groups/{group-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignGroup(assignGroup: AssignGroup, @Param("realm") realm: String, @Param("user-id") userId: UUID, @Param(
        "group-id"
    ) groupId: UUID)

    @RequestLine("DELETE /admin/realms/{realm}/users/{user-id}/groups/{group-id}")
    fun revokeGroup(@Param("realm") realm: String, @Param("user-id") userId: UUID, @Param("group-id") groupId: UUID)

    @RequestLine("POST /admin/realms/{realm}/groups/{group-id}/role-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignRealmRolesToGroup(roles: List<AssignRole>, @Param("realm") realm: String, @Param(
        "group-id"
    ) groupId: UUID)

    @RequestLine("POST /admin/realms/{realm}/users/{user-id}/role-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignClientRoles(roles: List<AssignRole>, @Param("realm") realm: String, @Param(
        "user-id"
    ) userId: UUID, @Param("client-id") clientId: UUID)

    @RequestLine("GET /admin/realms/{realm}/users/{user-id}/role-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun userClientRoles(@Param("realm") realm: String, @Param(
        "user-id"
    ) userId: UUID, @Param("client-id") clientId: UUID): List<RoleListItem>

    @RequestLine("POST /admin/realms/{realm}/groups/{group-id}/role-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignClientRolesToGroup(roles: List<AssignRole>, @Param("realm") realm: String, @Param(
        "group-id"
    ) groupId: UUID, @Param("client-id") clientId: UUID)

    @RequestLine("DELETE /admin/realms/{realm}/users/{user-id}/role-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun revokeRealmRoles(roles: List<AssignRole>, @Param("realm") realm: String, @Param("user-id") userId: UUID)

    @RequestLine("DELETE /admin/realms/{realm}/groups/{group-id}/role-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun revokeRealmRolesFromGroup(roles: List<AssignRole>, @Param("realm") realm: String, @Param(
        "group-id"
    ) groupId: UUID)

    @RequestLine("DELETE /admin/realms/{realm}/users/{user-id}/role-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun revokeClientRoles(roles: List<AssignRole>, @Param("realm") realm: String, @Param(
        "user-id"
    ) userId: UUID, @Param("client-id") clientId: UUID)

    @RequestLine("DELETE /admin/realms/{realm}/groups/{group-id}/role-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun revokeClientRolesFromGroup(roles: List<AssignRole>, @Param("realm") realm: String, @Param(
        "group-id"
    ) groupId: UUID, @Param("client-id") clientId: UUID)

    @RequestLine("GET /admin/realms/{realm}/roles")
    fun roles(@Param("realm") realm: String): List<RoleListItem>

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/roles")
    fun clientRoles(@Param("realm") realm: String, @Param("client-id") clientId: UUID): List<RoleListItem>

    @RequestLine("POST /admin/realms/{realm}/roles")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addRole(addRole: AddRole, @Param("realm") realm: String)

    @RequestLine("POST /admin/realms/{realm}/clients/{client-id}/roles")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addClientRole(addRole: AddRole, @Param("client-id") clientId: UUID, @Param("realm") realm: String)

    @RequestLine("GET /admin/realms/{realm}/roles-by-id/{role-id}")
    fun role(@Param("role-id") roleId: UUID, @Param("realm") realm: String): Role

    @RequestLine("GET /admin/realms/{realm}/roles-by-id/{role-id}?client={client-id}")
    fun clientRole(@Param("role-id") roleId: UUID, @Param("realm") realm: String, @Param(
        "client-id"
    ) clientId: UUID): Role

    @RequestLine("PUT /admin/realms/{realm}/roles-by-id/{role-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateRole(role: Role, @Param("role-id") roleId: UUID, @Param("realm") realm: String)

    @RequestLine("DELETE /admin/realms/{realm}/roles-by-id/{role-id}")
    fun deleteRole(@Param("role-id") roleId: UUID, @Param("realm") realm: String)

    @RequestLine("POST /admin/realms/{realm}/roles-by-id/{role-id}/composites")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addCompositeToRole(roles: List<RoleListItem>,
        @Param("role-id") roleId: UUID,
        @Param("realm") realm: String): Response

    @RequestLine("GET /admin/realms/{realm}/roles-by-id/{role-id}/composites")
    fun getCompositeChildRoles(@Param("role-id") roleId: UUID, @Param("realm") realm: String): List<RoleListItem>

    @RequestLine("GET /admin/realms/{realm}/roles/{name}")
    fun roleByName(@Param("name") name: String, @Param("realm") realm: String): Role

    @RequestLine("GET /admin/realms/{realm}/roles/{name}")
    fun roleByNameResponse(@Param("name") name: String, @Param("realm") realm: String): Response

    @RequestLine("GET /admin/realms/{realm}/clients")
    fun clients(@Param("realm") realm: String): List<ClientListItem>

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}")
    fun client(@Param("client-id") clientId: UUID, @Param("realm") realm: String): Client

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/client-secret")
    fun clientSecret(@Param("client-id") clientId: UUID, @Param("realm") realm: String): ClientSecret

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/service-account-user")
    fun clientServiceAccount(@Param("client-id") clientId: UUID, @Param("realm") realm: String): User

    @RequestLine("POST /admin/realms/{realm}/clients")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addSimpleClient(addSimpleClient: AddSimpleClient, @Param("realm") realm: String): Response

    @RequestLine("POST /admin/realms/{realm}/clients")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun importClient(importClientRepresentation: ImportClientRepresentation, @Param("realm") realm: String): Response

    @RequestLine("POST /admin/realms/{realm}/clients")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addClient(addClient: AddClient, @Param("realm") realm: String): Response

    @RequestLine("DELETE /admin/realms/{realm}/clients/{client-id}")
    fun deleteClient(@Param("client-id") clientId: UUID, @Param("realm") realm: String)

    @RequestLine("PUT /admin/realms/{realm}/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateClient(@Param("client-id") clientId: UUID, updateClient: Client, @Param("realm") realm: String): Response

    @Deprecated("Will be removed in a future release. Use addClientMapper action instead")
    @RequestLine("POST /admin/realms/{realm}/clients/{client-id}/protocol-mappers/models")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addMapper(@Param("client-id") clientId: UUID, addMapper: AddMapper, @Param("realm") realm: String): Response

    @Deprecated("Will be removed in a future release. Use deleteClientMapper action instead")
    @RequestLine("DELETE /admin/realms/{realm}/clients/{client-id}/protocol-mappers/models/{mapper-id}")
    fun deleteMapper(@Param("client-id") clientId: UUID,
        @Param("mapper-id") mapperId: UUID,
        @Param("realm") realm: String)

    @Deprecated("Will be removed in a future release. Use clientMappers action instead")
    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/protocol-mappers/models")
    fun mappers(@Param("client-id") clientId: UUID, @Param("realm") realm: String): List<Mapper>

    @RequestLine("POST /admin/realms/{realm}/clients/{client-id}/protocol-mappers/models")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addClientMapper(@Param("client-id") clientId: UUID,
        addMapper: AddMapper,
        @Param("realm") realm: String): Response

    @RequestLine("DELETE /admin/realms/{realm}/clients/{client-id}/protocol-mappers/models/{mapper-id}")
    fun deleteClientMapper(@Param("client-id") clientId: UUID,
        @Param("mapper-id") mapperId: UUID,
        @Param("realm") realm: String)

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/protocol-mappers/models")
    fun clientMappers(@Param("client-id") clientId: UUID, @Param("realm") realm: String): List<Mapper>

    @RequestLine("POST /admin/realms/{realm}/client-scopes/{client-scope-id}/protocol-mappers/models")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addClientScopeMapper(@Param("client-scope-id") clientScopeId: UUID,
        addMapper: AddMapper,
        @Param("realm") realm: String): Response

    @RequestLine("DELETE /admin/realms/{realm}/client-scopes/{client-scope-id}/protocol-mappers/models/{mapper-id}")
    fun deleteClientScopeMapper(@Param("client-scope-id") clientScopeId: UUID,
        @Param("mapper-id") mapperId: UUID,
        @Param(
            "realm"
        ) realm: String)

    @RequestLine("GET /admin/realms/{realm}/client-scopes/{client-scope-id}/protocol-mappers/models")
    fun clientScopeMappers(@Param("client-scope-id") clientScopeId: UUID, @Param("realm") realm: String): List<Mapper>

    @RequestLine("GET /admin/realms/{realm}/client-scopes")
    fun clientScopes(@Param("realm") realm: String): List<ClientScope>

    @RequestLine("POST /admin/realms/{realm}/client-scopes")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addClientScope(@Param("realm") realm: String, addClientScope: AddClientScope): Response

    @RequestLine("DELETE /admin/realms/{realm}/client-scopes/{client-scope-id}")
    fun deleteClientScope(@Param("realm") realm: String, @Param("client-scope-id") clientScopeId: UUID): Response

    @RequestLine("GET /admin/realms/{realm}/client-scopes/{client-scope-id}/scope-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun clientScopeRealmRoles(@Param("realm") realm: String,
        @Param("client-scope-id") clientScopeId: UUID): List<RoleListItem>

    @RequestLine("POST /admin/realms/{realm}/client-scopes/{client-scope-id}/scope-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignRealmRoleToClientScope(
        roles: List<AssignRole>, @Param("realm") realm: String,
        @Param("client-scope-id") clientScopeId: UUID,
    )

    @RequestLine("DELETE /admin/realms/{realm}/client-scopes/{client-scope-id}/scope-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun revokeRealmRoleFromClientScope(roles: List<AssignRole>, @Param("realm") realm: String,
        @Param("client-scope-id") clientScopeId: UUID): Response

    @RequestLine("GET /admin/realms/{realm}/client-scopes/{client-scope-id}/scope-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun clientScopeClientRoles(@Param("realm") realm: String, @Param("client-scope-id") clientScopeId: UUID,
        @Param("client-id") clientId: UUID): List<RoleListItem>

    @RequestLine("POST /admin/realms/{realm}/client-scopes/{client-scope-id}/scope-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignClientRoleToClientScope(roles: List<AssignRole>, @Param("realm") realm: String,
        @Param("client-scope-id") clientScopeId: UUID, @Param("client-id") clientId: UUID): Response

    @RequestLine("DELETE /admin/realms/{realm}/client-scopes/{client-scope-id}/scope-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun revokeClientRoleFromClientScope(roles: List<AssignRole>, @Param("realm") realm: String,
        @Param("client-scope-id") clientScopeId: UUID, @Param("client-id") clientId: UUID): Response

    @RequestLine("PUT /admin/realms/{realm}/clients/{client-id}/default-client-scopes/{client-scope-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignDefaultClientScope(@Param("realm") realm: String, @Param("client-id") clientId: UUID,
        @Param("client-scope-id") clientScopeId: UUID,
        assignClientScope: AssignClientScope): Response

    @RequestLine("DELETE /admin/realms/{realm}/clients/{client-id}/default-client-scopes/{client-scope-id}")
    fun withdrawDefaultClientScope(@Param("realm") realm: String, @Param("client-id") clientId: UUID,
        @Param("client-scope-id") clientScopeId: UUID): Response

    @RequestLine("PUT /admin/realms/{realm}/clients/{client-id}/optional-client-scopes/{client-scope-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignOptionalClientScope(@Param("realm") realm: String, @Param("client-id") clientId: UUID,
        @Param("client-scope-id") clientScopeId: UUID,
        assignClientScope: AssignClientScope): Response

    @RequestLine("DELETE /admin/realms/{realm}/clients/{client-id}/optional-client-scopes/{client-scope-id}")
    fun withdrawOptionalClientScope(@Param("realm") realm: String, @Param("client-id") clientId: UUID,
        @Param("client-scope-id") clientScopeId: UUID): Response

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/default-client-scopes")
    fun defaultClientScopes(@Param("realm") realm: String, @Param("client-id") clientId: UUID): List<ClientScopeItem>

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/optional-client-scopes")
    fun optionalClientScopes(@Param("realm") realm: String, @Param("client-id") clientId: UUID): List<ClientScopeItem>

    @RequestLine("GET /admin/realms/{realm}/components?parent={realm}&type=org.keycloak.storage.UserStorageProvider")
    fun userFederations(@Param("realm") realm: String): List<UserFederation>

    @RequestLine("GET /admin/realms/{realm}/groups/{group-id}")
    fun group(@Param("realm") realm: String, @Param("group-id") groupId: UUID): Group

    @RequestLine("POST /admin/realms/{realm}/groups")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addGroup(addGroup: AddGroup, @Param("realm") realm: String): Response

    @RequestLine("POST /admin/realms/{realm}/groups/{parent}/children")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addChildGroup(addGroup: AddGroup, @Param("parent") parentGroupId: UUID, @Param("realm") realm: String): Response

    @RequestLine("DELETE /admin/realms/{realm}/groups/{group-id}")
    fun deleteGroup(@Param("group-id") groupUUID: UUID, @Param("realm") realm: String): Response

    @RequestLine("GET /admin/realms/{realm}/groups?search={search}")
    fun searchGroup(@Param("search") search: String, @Param("realm") realm: String): List<GroupListItem>

    @RequestLine("PUT /admin/realms/{realm}/groups/{group-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateGroup(updateGroup: UpdateGroup, @Param("realm") realm: String, @Param("group-id") groupId: UUID): Response

    @RequestLine("POST /admin/realms/{realm}/components")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addUserFederation(addUserFederation: AddUserFederation, @Param("realm") realm: String)

    @RequestLine("DELETE /admin/realms/{realm}/components/{user-federation-id}")
    fun deleteUserFederation(@Param("realm") realm: String, @Param("user-federation-id") userFederationId: UUID)

    @RequestLine("POST /admin/realms/{realm}/components")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addUserFederationMapper(addUserFederationMapper: AddUserFederationMapper, @Param(
        "realm"
    ) realm: String): Response

    @RequestLine(
        "GET /admin/realms/{realm}/components" +
                "?parent={ldap-id}&type=org.keycloak.storage.ldap.mappers.LDAPStorageMapper"
    )
    fun ldapMappers(@Param("realm") realm: String, @Param("ldap-id") ldapId: UUID)
            : List<UserFederationMapper>

    @RequestLine("DELETE /admin/realms/{realm}/components/{mapperId}")
    fun deleteUserFederationMapper(@Param("realm") realm: String, @Param("mapperId") mapperId: UUID)
            : Response

    @RequestLine("POST /admin/realms/")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addRealm(addRealm: AddRealm)

    @RequestLine("PUT /admin/realms/{id}/ui-ext")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateRealm(@Param("id") realmId: String, realm: Realm)

    @RequestLine("DELETE /admin/realms/{realm-id}")
    fun deleteRealm(@Param("realm-id") id: String)

    @RequestLine("GET /admin/realms/{realm}/users/{user-id}/role-mappings/realm/composite")
    fun userRoles(@Param("realm") realm: String, @Param("user-id") id: UUID): List<RoleListItem>

    @RequestLine("GET /admin/realms/{realm}/users/{user-id}/groups")
    fun userGroups(@Param("realm") realm: String, @Param("user-id") id: UUID): List<UserGroupListItem>

    @RequestLine("GET /admin/realms/{realm}/groups/{group-id}/role-mappings/realm/composite")
    fun groupRoles(@Param("realm") realm: String, @Param("group-id") id: UUID): List<RoleListItem>

    @RequestLine("GET /admin/realms/{realm}/groups/{group-id}/role-mappings/clients/{client-id}/composite")
    fun groupClientRoles(@Param("realm") realm: String, @Param("group-id") id: UUID,
         @Param("client-id") clientId: UUID): List<RoleListItem>

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/scope-mappings/realm")
    fun realmRoleScopeMappingsOfClient(@Param("realm") realm: String, @Param("client-id") id: UUID): List<RoleListItem>

    @RequestLine("POST /admin/realms/{realm}/clients/{client-id}/scope-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addRealmRoleScopeMappingToClient(roleScopeMapping: List<RoleListItem>, @Param("realm") realm: String,
        @Param("client-id") id: UUID): Response

    @RequestLine("DELETE /admin/realms/{realm}/clients/{client-id}/scope-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun deleteRealmRoleScopeMappingOfClient(roleScopeMapping: List<RoleListItem>, @Param("realm") realm: String,
        @Param("client-id") id: UUID): Response

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/scope-mappings/clients/{role-client-id}")
    fun clientRoleScopeMappingsOfClient(@Param("realm") realm: String, @Param("client-id") id: UUID,
        @Param("role-client-id") roleClientId: UUID): List<RoleListItem>

    @RequestLine("POST /admin/realms/{realm}/clients/{client-id}/scope-mappings/clients/{role-client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addClientRoleScopeMappingToClient(roleScopeMapping: List<RoleListItem>, @Param("realm") realm: String,
        @Param("client-id") id: UUID,
        @Param("role-client-id") roleClientId: UUID): Response

    @RequestLine("DELETE /admin/realms/{realm}/clients/{client-id}/scope-mappings/clients/{role-client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun deleteClientRoleScopeMappingOfClient(roleScopeMapping: List<RoleListItem>, @Param("realm") realm: String,
        @Param("client-id") id: UUID,
        @Param("role-client-id") roleClientId: UUID): Response

    /**
     * Sends an email with the specified actions to the user if smtp is configured for this realm
     * @param lifespan in seconds, defaults to 12h
     */
    @RequestLine("PUT /admin/realms/{realm}/users/{user-id}/execute-actions-email?lifespan={lifespan}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun sendEmail(actions: List<EmailActions>, @Param("realm") realm: String, @Param("user-id") userId: UUID,
        @Param("lifespan") lifespan: Int = 43200)

    @RequestLine("GET /admin/realms/{realm}/identity-provider/instances")
    fun identityProviders(@Param("realm") realm: String): List<IdentityProvider>

    @RequestLine("POST /admin/realms/{realm}/identity-provider/instances")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addIdentityProvider(addIdentityProvider: AddIdentityProvider, @Param("realm") realm: String): Response

    @RequestLine("PUT /admin/realms/{realm}/identity-provider/instances/{alias}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateIdentityProvider(updateIdentityProvider: UpdateIdentityProvider,
        @Param("realm") realm: String,
        @Param("alias") alias: String): Response

    @RequestLine("GET /admin/realms/{realm}/identity-provider/instances/{alias}")
    fun identityProvider(@Param("realm") realm: String, @Param("alias") alias: String): IdentityProvider

    @RequestLine("DELETE /admin/realms/{realm}/identity-provider/instances/{alias}")
    fun deleteIdentityProvider(@Param("realm") realm: String, @Param("alias") alias: String)

    @RequestLine("POST /admin/realms/{realm}/identity-provider/instances/{alias}/mappers")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addIdentityProviderMapper(addIdentityProviderMapper: AddIdentityProviderMapper,
        @Param("realm") realm: String,
        @Param("alias") alias: String): Response

    @RequestLine("GET /admin/realms/{realm}/identity-provider/instances/{alias}/mappers/{name}")
    fun identityProviderMapper(@Param("realm") realm: String,
        @Param("alias") alias: String,
        @Param("name") name: String): IdentityProviderMapper

    @RequestLine("DELETE /admin/realms/{realm}/identity-provider/instances/{alias}/mappers/{id}")
    fun deleteIdentityProviderMapper(@Param("realm") realm: String,
        @Param("alias") alias: String,
        @Param("id") id: String)

    @RequestLine("GET /admin/realms/{realm}/identity-provider/instances/{alias}/mappers")
    fun identityProviderMappers(@Param("realm") realm: String,
        @Param("alias") alias: String): List<IdentityProviderMapper>

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("POST /admin/realms/{realm}/authentication/flows")
    fun addFlow(@Param("realm") realm: String, addFlow: AddFlow): Response

    @RequestLine("GET /admin/realms/{realm}/authentication/flows")
    fun flows(@Param("realm") realm: String): List<Flow>

    @RequestLine("GET /admin/realms/{realm}/authentication/flows/{alias}/executions")
    fun flowExecutions(@Param("realm") realm: String, @Param("alias") alias: String): List<AuthenticationExecution>

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("PUT /admin/realms/{realm}/authentication/flows/{id}")
    fun updateFlow(@Param("realm") realm: String, @Param("id") flowId: UUID, updateFlow: UpdateFlow)

    @RequestLine("DELETE /admin/realms/{realm}/authentication/flows/{id}")
    fun deleteFlow(@Param("realm") realm: String, @Param("id") flowId: UUID)

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("POST /admin/realms/{realm}/authentication/flows/{flowAlias}/copy")
    fun copyFlow(@Param("realm") realm: String, @Param("flowAlias") flowAlias: String, body: String): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("POST /admin/realms/{realm}/authentication/flows/{alias}/executions/execution")
    fun addFlowExecution(@Param("realm") realm: String,
        @Param("alias") alias: String,
        addFlowExecution: AddFlowExecution): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("PUT /admin/realms/{realm}/authentication/flows/{alias}/executions")
    fun updateFlowExecution(@Param("realm") realm: String,
        @Param("alias") alias: String,
        updateFlowExecution: UpdateFlowExecution): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("POST /admin/realms/{realm}/authentication/executions/{executionId}/config")
    fun updateFlowExecutionWithNewConfiguration(@Param("realm") realm: String,
        @Param("executionId") executionId: String,
        authenticatorConfig: AuthenticatorConfig): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("GET /admin/realms/{realm}/authentication/config/{configurationId}")
    fun getAuthenticatorConfiguration(@Param("realm") realm: String,
        @Param("configurationId") configurationId: String): AuthenticatorConfig

    @RequestLine("DELETE /admin/realms/{realm}/authentication/executions/{id}")
    fun deleteFlowExecution(@Param("realm") realm: String, @Param("id") executionId: UUID)

    @RequestLine("GET /admin/realms/{realm}/authentication/required-actions")
    fun requiredActions(@Param("realm") realm: String): List<RequiredActionProviderItem>

    @RequestLine("GET /admin/realms/{realm}/authentication/required-actions/{alias}")
    fun requiredAction(@Param("realm") realm: String,
        @Param("alias") alias: String): RequiredActionProviderItem

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("PUT /admin/realms/{realm}/authentication/required-actions/{alias}")
    fun updateRequiredAction(@Param("realm") realm: String,
        @Param("alias") alias: String,
        requiredActionProviderItem: RequiredActionProviderItem): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("DELETE /admin/realms/{realm}/authentication/required-actions/{alias}")
    fun deleteRequiredAction(@Param("realm") realm: String,
        @Param("alias") alias: String): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("PUT /admin/realms/{realm}/users/profile")
    fun updateRealmProfile(
        @Param("realm") realm: String,
        profile: RealmProfile
    ): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("GET /admin/realms/{realm}/localization/{locale}")
    fun getLocalizationEntries(
        @Param("realm") realm: String,
        @Param("locale") locale: String,
    ): Map<String, String>

    @Headers("Content-Type: text/plain; charset=utf-8")
    @RequestLine("PUT /admin/realms/{realm}/localization/{locale}/{key}")
    fun updateLocalizationEntry(
        @Param("realm") realm: String,
        @Param("locale") locale: String,
        @Param("key") key: String,
        text: String
    ): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("DELETE /admin/realms/{realm}/localization/{locale}/{key}")
    fun deleteLocalizationEntry(
        @Param("realm") realm: String,
        @Param("locale") locale: String,
        @Param("key") key: String
    ): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("GET /admin/realms/{realm}/organizations")
    fun organizations(@Param("realm") realm: String): List<Organization>

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("POST /admin/realms/{realm}/organizations")
    fun addOrganization(
        @Param("realm") realm: String,
        organization: AddOrganization
    ): Response

    @Headers("Content-Type: application/json; charset=utf-8")
    @RequestLine("DELETE /admin/realms/{realm}/organizations/{id}")
    fun deleteOrganization(
        @Param("id") id: UUID,
        @Param("realm") realm: String,
    ): Response
}

