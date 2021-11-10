package de.klg71.keycloakmigration.keycloakapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import feign.Feign
import feign.Logger
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder

/**
 * Builds the [KeycloakClient]
 * @param baseUrl base url of the keycloak instance e.g. http://localhost:8080/auth
 * @param adminUser user to execute the migrations. executed migrations are stored in the user-attributes as hashes
 * @param adminPassword password of the user to execute the migrations
 * @param realm Realm to use for the login of the user
 * @param clientId id of the client to use for the login of the user
 */
fun initKeycloakClient(baseUrl: String, adminUser: String, adminPassword: String, realm: String,
    clientId: String, logger: Logger? = null, totp: String = "", tokenOffsetMs: Long = 1000) = initObjectMapper().let {
    TokenHolder(
        initKeycloakLoginClient(baseUrl, logger),
        adminUser, adminPassword, realm, clientId, totp, tokenOffsetMs
    ).let {
        initKeycloakClientWithTokenHolder(baseUrl, logger, it)
    }
}

internal fun initKeycloakClientWithTokenHolder(baseUrl: String, logger: Logger? = null, tokenHolder: TokenHolder) =
    Feign.builder().run {
        val objectMapper = initObjectMapper()
        encoder(JacksonEncoder(objectMapper))
        decoder(JacksonDecoder(objectMapper.apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }))
        requestInterceptor {
            tokenHolder.token().run {
                it.header("Authorization", "Bearer $accessToken")
            }
        }
        logger?.let {
            logger(it)
        }
        logLevel(Logger.Level.FULL)
        target(KeycloakClient::class.java, baseUrl)
    }!!

/**
 * Builds the [KeycloakLoginClient]
 * Only build this if you just need a token and don't need any other keycloak resources.
 * [initKeycloakClient] automatically acquires tokens.
 * @param baseUrl base url of the keycloak instance e.g. http://localhost:8080/auth
 */
fun initKeycloakLoginClient(baseUrl: String, logger: Logger? = null): KeycloakLoginClient =
    initObjectMapper().let {
        initKeycloakLoginClient(it, baseUrl, logger)
    }

internal fun initKeycloakLoginClient(objectMapper: ObjectMapper,
    baseUrl: String, logger: Logger? = null): KeycloakLoginClient = Feign.builder().run {
    encoder(FormEncoder(JacksonEncoder(objectMapper)))
    decoder(JacksonDecoder(objectMapper))
    logger?.let {
        logger(it)
    }
    logLevel(Logger.Level.FULL)
    target(KeycloakLoginClient::class.java, baseUrl)
}

private fun initObjectMapper() = ObjectMapper().registerModule(KotlinModule())!!
