package de.klg71.keycloakmigration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.changeControl.Action
import de.klg71.keycloakmigration.changeControl.ActionDeserializer
import de.klg71.keycloakmigration.changeControl.ChangeFile
import de.klg71.keycloakmigration.model.AccessToken
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
import org.slf4j.LoggerFactory

val myModule = module {
    single("default") { initObjectMapper() }
    single("yamlObjectMapper") { initYamlObjectMapper() }
    single { initKeycloakLoginClient(get("default")) }
    single { TokenHolder(get()) }
    single { initFeignClient(get("default"), get()) }
}

fun kotlinObjectMapper () = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())!!

fun initYamlObjectMapper(): ObjectMapper = ObjectMapper(YAMLFactory()).registerModule(actionModule(kotlinObjectMapper())).registerModule(KotlinModule())!!

fun actionModule(objectMapper: ObjectMapper) = SimpleModule().addDeserializer(Action::class.java, ActionDeserializer(objectMapper))!!

fun initKeycloakLoginClient(objectMapper: ObjectMapper): KeycloakLoginClient = Feign.builder().run {
    encoder(FormEncoder(JacksonEncoder(objectMapper)))
    decoder(JacksonDecoder(objectMapper))
    logger(Slf4jLogger())
    logLevel(Logger.Level.BASIC)
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

    private val yamlObjectMapper by inject<ObjectMapper>(name = "yamlObjectMapper")

    companion object {
        val LOG = LoggerFactory.getLogger(KeycloakMigration::class.java)
    }

    init {
        val loader = Thread.currentThread().contextClassLoader
        val set = yamlObjectMapper.readValue<ChangeFile>(loader.getResourceAsStream("testMigration/changesets/initial.yml"))

        val executedChanges = mutableListOf<Action>()
        try {
            set.keycloakChangeSet.changes.forEach {
                if (it.isRequired(client)) {
                    it.execute(client)
                    executedChanges.add(it)
                } else {
                    LOG.info("Skipping migration: ${it.name()}")
                }
            }
        } catch (e: Exception) {
            LOG.error("Error occured while migrating: ", e)
            LOG.error("Rolling back changes", e)
            executedChanges.forEach {
                it.undo(client)
            }
        }

    }
}
