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
import de.klg71.keycloakmigration.rest.KeycloakClient
import de.klg71.keycloakmigration.rest.KeycloakLoginClient
import de.klg71.keycloakmigration.rest.userByName
import feign.Feign
import feign.Logger
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun myModule(adminUser: String, adminPassword: String, baseUrl: String, realm: String, clientId: String) = module {
    single(named("default")) { initObjectMapper() }
    single(named("yamlObjectMapper")) { initYamlObjectMapper() }
    single { initKeycloakLoginClient(get(named("default")), baseUrl) }
    single { TokenHolder(get<KeycloakLoginClient>(), adminUser, adminPassword, realm, clientId) }
    single { initFeignClient(get(named("default")), get(), baseUrl) }
    single(named("migrationUserId")) { loadCurrentUser(get(), adminUser, realm) }
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
    logLevel(Logger.Level.FULL)
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

private fun loadCurrentUser(client: KeycloakClient, userName: String, realm: String) = client.userByName(userName, realm).id
