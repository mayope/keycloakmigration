package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.AccessToken
import feign.Headers
import feign.Param
import feign.RequestLine

interface KeycloakLoginClient {
    @RequestLine("POST /realms/{realm}/protocol/openid-connect/token")
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    fun login(@Param("realm") realm: String,
              @Param("grant_type") grantType: String,
              @Param("client_id") clientId: String,
              @Param("username") username: String,
              @Param("password") password: String): AccessToken

    @RequestLine("POST /realms/{realm}/protocol/openid-connect/token")
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    fun login(@Param("realm") realm: String,
              @Param("grant_type") grantType: String,
              @Param("refresh_token") refreshToken: String,
              @Param("client_id") clientId: String): AccessToken
}
