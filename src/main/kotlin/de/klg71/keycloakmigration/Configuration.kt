package de.klg71.keycloakmigration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
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

fun myModule(adminUser: String, adminPassword: String, baseUrl: String, realm: String) = module {
    single("default") { initObjectMapper() }
    single("yamlObjectMapper") { initYamlObjectMapper() }
    single { initKeycloakLoginClient(get("default"), baseUrl) }
    single { TokenHolder(get(), adminUser, adminPassword, realm) }
    single { initFeignClient(get("default"), get(), baseUrl) }
    single("migrationUserId") { loadCurrentUser(get()) }
}


private class TokenHolder(client: KeycloakLoginClient, adminUser: String, adminPassword: String, realm: String) {
    val token: AccessToken = client.login(realm, "password", "admin-cli", adminUser, adminPassword)
}

private fun kotlinObjectMapper() = ObjectMapper(YAMLFactory()).apply {
    enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
    registerModule(KotlinModule())!!
    enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING)
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
}

private fun initYamlObjectMapper(): ObjectMapper = ObjectMapper(YAMLFactory())
        .registerModule(actionModule(kotlinObjectMapper())).registerModule(KotlinModule())!!

private fun actionModule(objectMapper: ObjectMapper) = SimpleModule()
        .addDeserializer(Action::class.java, ActionDeserializer(objectMapper))!!

private fun initKeycloakLoginClient(objectMapper: ObjectMapper, baseUrl: String): KeycloakLoginClient = Feign.builder().run {
    encoder(FormEncoder(JacksonEncoder(objectMapper)))
    decoder(JacksonDecoder(objectMapper))
    logger(Slf4jLogger())
    logLevel(Logger.Level.NONE)
    target(KeycloakLoginClient::class.java, baseUrl)
}

private fun initFeignClient(objectMapper: ObjectMapper, tokenHolder: TokenHolder, baseUrl: String) = Feign.builder().run {
    encoder(JacksonEncoder(objectMapper))
    decoder(JacksonDecoder(objectMapper))
    requestInterceptor {
        tokenHolder.token.run {
            it.header("Authorization", "Bearer $accessToken")
        }
    }
    logger(Slf4jLogger())
    logLevel(Logger.Level.FULL)
    target(KeycloakClient::class.java, baseUrl)
}!!

private fun initObjectMapper() = ObjectMapper().registerModule(KotlinModule())!!

private fun loadCurrentUser(client: KeycloakClient) = client.userByName("admin", "master").id
