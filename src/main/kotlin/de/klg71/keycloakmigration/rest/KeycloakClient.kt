package de.klg71.keycloakmigration.rest

import de.klg71.keycloakmigration.model.*
import feign.Headers
import feign.Param
import feign.RequestLine
import feign.Response
import java.util.*

interface KeycloakClient {
    @RequestLine("GET /admin/realms")
    fun realms(): List<Realm>

    @RequestLine("GET /admin/realms/{realm}/users")
    fun users(@Param("realm") realm: String): List<UserListItem>

    @RequestLine("GET /admin/realms/{realm}/users?username={username}")
    fun searchByUsername(@Param("username") username: String, @Param("realm") realm: String): List<UserListItem>

    @RequestLine("GET /admin/realms/{realm}/users?search={search}")
    fun searchUser(@Param("search") search: String, @Param("realm") realm: String): List<UserListItem>

    @RequestLine("DELETE /admin/realms/{realm}/users/{user-id}")
    fun deleteUser(@Param("user-id") userId: UUID, @Param("realm") realm: String)

    @RequestLine("POST /admin/realms/{realm}/users")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addUser(user: AddUser, @Param("realm") realm: String): Response

    @RequestLine("GET /admin/realms/{realm}/users/{user-id}")
    fun user(@Param("user-id") userId: UUID, @Param("realm") realm: String): User

    @RequestLine("PUT /admin/realms/{realm}/users/{user-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateUser(@Param("user-id") userId: UUID, user: User, @Param("realm") realm: String)

    @RequestLine("POST /admin/realms/{realm}/users/{user-id}/role-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignRealmRoles(roles: List<AssignRole>, @Param("realm") realm: String, @Param("user-id") userId: UUID)

    @RequestLine("POST /admin/realms/{realm}/users/{user-id}/role-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun assignClientRoles(roles: List<AssignRole>, @Param("realm") realm: String, @Param("user-id") userId: UUID, @Param("client-id") clientId: UUID)

    @RequestLine("DELETE /admin/realms/{realm}/users/{user-id}/role-mappings/realm")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun removeRealmRoles(roles: List<AssignRole>, @Param("realm") realm: String, @Param("user-id") userId: UUID)

    @RequestLine("DELETE /admin/realms/{realm}/users/{user-id}/role-mappings/clients/{client-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun removeClientRoles(roles: List<AssignRole>, @Param("realm") realm: String, @Param("user-id") userId: UUID, @Param("client-id") clientId: UUID)

    @RequestLine("GET /admin/realms/{realm}/roles")
    fun roles(@Param("realm") realm: String): List<RoleListItem>

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}/roles")
    fun clientRoles(@Param("realm") realm: String,@Param("client-id") clientId:UUID): List<RoleListItem>

    @RequestLine("POST /admin/realms/{realm}/roles")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addRole(addRole: AddRole, @Param("realm") realm: String)

    @RequestLine("GET /admin/realms/{realm}/roles-by-id/{role-id}")
    fun role(@Param("role-id") roleId: UUID, @Param("realm") realm: String): Role

    @RequestLine("GET /admin/realms/{realm}/roles-by-id/{role-id}?client={client-id}")
    fun clientRole(@Param("role-id") roleId: UUID, @Param("realm") realm: String,@Param("client-id") clientId:UUID): Role

    @RequestLine("PUT /admin/realms/{realm}/roles-by-id/{role-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateRole(role: Role, @Param("role-id") roleId: UUID, @Param("realm") realm: String)

    @RequestLine("DELETE /admin/realms/{realm}/roles-by-id/{role-id}")
    fun deleteRole(@Param("role-id") roleId: UUID, @Param("realm") realm: String)

    @RequestLine("GET /admin/realms/{realm}/roles/{name}")
    fun roleByName(@Param("name") name: String, @Param("realm") realm: String): Role

    @RequestLine("GET /admin/realms/{realm}/roles/{name}")
    fun checkRoleByName(@Param("name") name: String, @Param("realm") realm: String): Response

    @RequestLine("GET /admin/realms/{realm}/clients")
    fun clients(@Param("realm") realm: String): List<ClientListItem>

    @RequestLine("GET /admin/realms/{realm}/clients/{client-id}")
    fun client(@Param("client-id") clientId: UUID, @Param("realm") realm: String): Client

    @RequestLine("POST /admin/realms/{realm}/clients")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addSimpleClient(addSimpleClient: AddSimpleClient, @Param("realm") realm: String): Response

    @RequestLine("POST /admin/realms/{realm}/clients")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addClient(addClient: AddClient, @Param("realm") realm: String): Response

    @RequestLine("DELETE /admin/realms/{realm}/clients/{client-id}")
    fun deleteClient(@Param("client-id") roleId: UUID, @Param("realm") realm: String)

    @RequestLine("GET /admin/realms/{realm}/components?parent={realm}&type=org.keycloak.storage.UserStorageProvider")
    fun userFederations(@Param("realm") realm: String): List<UserFederationItem>

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
}