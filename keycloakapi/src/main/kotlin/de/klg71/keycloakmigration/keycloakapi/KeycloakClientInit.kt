package de.klg71.keycloakmigration.keycloakapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import feign.Feign
import feign.Logger
import feign.RetryableException
import feign.Retryer
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.core.IntervalFunction
import io.github.resilience4j.feign.FeignDecorator
import io.github.resilience4j.feign.FeignDecorators
import io.github.resilience4j.feign.Resilience4jFeign
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import java.time.Duration

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
    Resilience4jFeign.builder(resilienceDecorator()).run {
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

private fun resilienceDecorator(): FeignDecorator {
    return FeignDecorators.builder().run {
        withRetry(Retry.of("keycloakRetry", retryDefaultConfig()))
        build()
    }
}
fun retryDefaultConfig(): RetryConfig {
    return RetryConfig.from<RetryConfig>(RetryConfig.ofDefaults()).run {
        retryOnException {
            it !is CallNotPermittedException
        }
        intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(1), 2.0))
        build()
    }
}

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
