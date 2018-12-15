package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.model.*
import feign.Headers
import feign.Param
import feign.RequestLine
import java.util.*

interface KeycloakLoginClient {
    @RequestLine("POST /realms/master/protocol/openid-connect/token")
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=UTF-8")
    fun login(@Param("grant_type") grantType: String,
              @Param("client_id") clientId: String,
              @Param("username") username: String,
              @Param("password") password: String): AccessToken

}