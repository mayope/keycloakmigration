package de.klg71.keycloakmigration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.klg71.keycloakmigration.model.AccessToken
import de.klg71.keycloakmigration.model.AddUser
import feign.Feign
import feign.Logger
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.koin.dsl.module.module
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject

val myModule = module {
    single { initObjectMapper() }
    single { initKeycloakLoginClient(get()) }
    single { TokenHolder(get()) }
    single { initFeignClient(get(), get()) }
}

fun initKeycloakLoginClient(objectMapper: ObjectMapper): KeycloakLoginClient = Feign.builder().run {
    encoder(FormEncoder(JacksonEncoder(objectMapper)))
    decoder(JacksonDecoder(objectMapper))
    logger(Slf4jLogger())
    logLevel(Logger.Level.FULL)
    target(KeycloakLoginClient::class.java, "http://localhost:8080/auth")
}

fun initFeignClient(objectMapper: ObjectMapper, tokenHolder: TokenHolder) = Feign.builder().run {
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

fun initObjectMapper() = ObjectMapper().registerModule(KotlinModule())!!


fun main(args: Array<String>) {

    startKoin(listOf(myModule))

    KeycloakMigration()

    stopKoin()
}

class TokenHolder(client: KeycloakLoginClient) {
    val token: AccessToken = client.login("password", "admin-cli", "admin", "admin")
}

class KeycloakMigration : KoinComponent {
    private val client by inject<KeycloakClient>()

    init {
        client.searchUsers("Lukas", "master").forEach { client.deleteUser(it.id, "master") }
        val addUser = AddUser("Lukas")
        client.addUser(addUser, "master")

        client.realms().forEach { println(it) }
        client.users("master").forEach { println(client.user(it.id, "master")) }
        client.roles("master").forEach { println(it) }
        client.clients("master").forEach { println(client.client(it.id, "master")) }
        client.userFederations("master").forEach { println(it) }
    }
}
