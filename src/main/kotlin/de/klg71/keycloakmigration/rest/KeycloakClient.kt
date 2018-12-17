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

    @RequestLine("GET /admin/realms/{realm}/roles")
    fun roles(@Param("realm") realm: String): List<RoleListItem>

    @RequestLine("POST /admin/realms/{realm}/roles")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun addRole(addRole: AddRole, @Param("realm") realm: String)

    @RequestLine("GET /admin/realms/{realm}/roles-by-id/{role-id}")
    fun role(@Param("role-id") roleId: UUID, @Param("realm") realm: String): Role

    @RequestLine("PUT /admin/realms/{realm}/roles-by-id/{role-id}")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun updateRole(role:Role, @Param("role-id") roleId: UUID, @Param("realm") realm: String)

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

    @RequestLine("GET /admin/realms/{realm}/components?parent={realm}&type=org.keycloak.storage.UserStorageProvider")
    fun userFederations(@Param("realm") realm: String): List<UserFederationItem>
}