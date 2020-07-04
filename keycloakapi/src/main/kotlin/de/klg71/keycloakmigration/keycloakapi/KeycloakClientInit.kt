package de.klg71.keycloakmigration.keycloakapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import feign.Feign
import feign.Logger
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger

/**
 * Builds the [KeycloakClient]
 * @param baseUrl base url of the keycloak instance e.g. http://localhost:8080/auth
 * @param adminUser user to execute the migrations. executed migrations are stored in the user-attributes as hashes
 * @param adminPassword password of the user to execute the migrations
 * @param realm Realm to use for the login of the user
 * @param clientId id of the client to use for the login of the user
 */
fun initKeycloakClient(baseUrl: String, adminUser: String, adminPassword: String, realm: String,
                       clientId: String) = Feign.builder().run {
    val objectMapper = initObjectMapper()
    val tokenHolder = TokenHolder(initKeycloakLoginClient(objectMapper, baseUrl), adminUser, adminPassword, realm,
            clientId)
    encoder(JacksonEncoder(objectMapper))
    decoder(JacksonDecoder(objectMapper.apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }))
    requestInterceptor {
        tokenHolder.token().run {
            it.header("Authorization", "Bearer $accessToken")
        }
    }
    logger(Slf4jLogger())
    logLevel(Logger.Level.FULL)
    target(KeycloakClient::class.java, baseUrl)
}!!

/**
 * Builds the [KeycloakLoginClient]
 * Only build this if you just need a token and don't need any other keycloak resources.
 * [initKeycloakClient] automatically acquires tokens.
 * @param baseUrl base url of the keycloak instance e.g. http://localhost:8080/auth
 */
fun initKeycloakLoginClient(baseUrl: String): KeycloakLoginClient =
        initObjectMapper().let {
            initKeycloakLoginClient(it, baseUrl)
        }

internal fun initKeycloakLoginClient(objectMapper: ObjectMapper,
                                     baseUrl: String): KeycloakLoginClient = Feign.builder().run {
    encoder(FormEncoder(JacksonEncoder(objectMapper)))
    decoder(JacksonDecoder(objectMapper))
    logger(Slf4jLogger())
    logLevel(Logger.Level.FULL)
    target(KeycloakLoginClient::class.java, baseUrl)
}

private fun initObjectMapper() = ObjectMapper().registerModule(KotlinModule())!!
