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

internal fun initKeycloakLoginClient(objectMapper: ObjectMapper,
                                     baseUrl: String): KeycloakLoginClient = Feign.builder().run {
    encoder(FormEncoder(JacksonEncoder(objectMapper)))
    decoder(JacksonDecoder(objectMapper))
    logger(Slf4jLogger())
    logLevel(Logger.Level.FULL)
    target(KeycloakLoginClient::class.java, baseUrl)
}

fun initKeycloakLoginClient(baseUrl: String): KeycloakLoginClient =
        initObjectMapper().let {
            initKeycloakLoginClient(it, baseUrl)
        }

private fun initObjectMapper() = ObjectMapper().registerModule(KotlinModule())!!
