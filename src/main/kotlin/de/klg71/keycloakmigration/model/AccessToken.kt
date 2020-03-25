package de.klg71.keycloakmigration.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AccessToken(@JsonProperty("access_token") val accessToken: String,
                       @JsonProperty("expires_in") val expiresIn: Long,
                       @JsonProperty("refresh_expires_in") val refreshExpiresIn: Long,
                       @JsonProperty("refresh_token") val refreshToken: String,
                       @JsonProperty("token_type") val tokenType: String,
                       @JsonProperty("not-before-policy") val notBeforePolicy: Int,
                       @JsonProperty("session_state") val sessionState: String,
                       val scope: String)
