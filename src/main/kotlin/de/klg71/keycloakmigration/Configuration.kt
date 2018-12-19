package de.klg71.keycloakmigration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.klg71.keycloakmigration.changeControl.ActionDeserializer
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.model.AccessToken
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.KeycloakLoginClient
import de.klg71.keycloakmigration.rest.userByName
import feign.Feign
import feign.Logger
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.koin.dsl.module.module

val myModule = module {
    single("default") { initObjectMapper() }
    single("yamlObjectMapper") { initYamlObjectMapper() }
    single { initKeycloakLoginClient(get("default")) }
    single { TokenHolder(get()) }
    single { initFeignClient(get("default"), get()) }
    single("migrationUserId") { loadCurrentUser(get()) }
}


private class TokenHolder(client: KeycloakLoginClient) {
    val token: AccessToken = client.login("password", "admin-cli", "admin", "admin")
}

private fun kotlinObjectMapper() = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())!!

private fun initYamlObjectMapper(): ObjectMapper = ObjectMapper(YAMLFactory()).registerModule(actionModule(kotlinObjectMapper())).registerModule(KotlinModule())!!

private fun actionModule(objectMapper: ObjectMapper) = SimpleModule().addDeserializer(Action::class.java, ActionDeserializer(objectMapper))!!

private fun initKeycloakLoginClient(objectMapper: ObjectMapper): KeycloakLoginClient = Feign.builder().run {
    encoder(FormEncoder(JacksonEncoder(objectMapper)))
    decoder(JacksonDecoder(objectMapper))
    logger(Slf4jLogger())
    logLevel(Logger.Level.NONE)
    target(KeycloakLoginClient::class.java, "http://localhost:8080/auth")
}

private fun initFeignClient(objectMapper: ObjectMapper, tokenHolder: TokenHolder) = Feign.builder().run {
    encoder(JacksonEncoder(objectMapper))
    decoder(JacksonDecoder(objectMapper))
    requestInterceptor {
        tokenHolder.token.run {
            it.header("Authorization", "Bearer $accessToken")
        }
    }
    logger(Slf4jLogger())
    logLevel(Logger.Level.FULL)
    target(KeycloakClient::class.java, "http://localhost:8080/auth")
}!!

private fun initObjectMapper() = ObjectMapper().registerModule(KotlinModule())!!

private fun loadCurrentUser(client: KeycloakClient) = client.userByName("admin", "master").id
