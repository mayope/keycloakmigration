package de.klg71.keycloakmigration

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.klg71.keycloakmigration.changeControl.RealmChecker
import de.klg71.keycloakmigration.changeControl.StringEnvSubstitutor
import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.changeControl.actions.ActionDeserializer
import de.klg71.keycloakmigration.changeControl.actions.ActionFactory
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.initKeycloakClient
import de.klg71.keycloakmigration.keycloakapi.userByName
import feign.Logger
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Initialize dependency injection and create the beans according to the given parameters
 */
@Suppress("LongParameterList")
fun myModule(adminUser: String,
    adminPassword: String,
    adminTotp: String,
    adminUseOauth: Boolean,
    adminUseOauthLocalPort: Int,
    baseUrl: String,
    realm: String,
    clientId: String,
    parameters: Map<String, String>,
    failOnUndefinedVariabled: Boolean,
    warnOnUndefinedVariables: Boolean,
    logger: Logger? = null) = module {
    single { StringEnvSubstitutor(failOnUndefinedVariabled, warnOnUndefinedVariables) }
    single(named("yamlObjectMapper")) { initYamlObjectMapper() }
    single(named("parameters")) { parameters }
    single {
        initKeycloakClient(
            baseUrl, adminUser, adminPassword, adminUseOauth, adminUseOauthLocalPort, realm, clientId, logger, adminTotp
        )
    }
    single(named("migrationUserId")) { loadCurrentUser(get(), adminUser, realm) }
    single { RealmChecker() }
}

private fun kotlinObjectMapper() = ObjectMapper(YAMLFactory()).apply {
    enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
    registerModule(KotlinModule.Builder().build())!!
    enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING)
    propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
}

private fun initYamlObjectMapper(): ObjectMapper =
    ObjectMapper(YAMLFactory()).registerModule(actionModule(initActionFactory(kotlinObjectMapper())))
        .registerModule(KotlinModule.Builder().build())!!

private fun actionModule(actionFactory: ActionFactory) = SimpleModule().addDeserializer(
        Action::class.java, ActionDeserializer(actionFactory)
    )!!

private fun initActionFactory(objectMapper: ObjectMapper) = ActionFactory(
    objectMapper
)

private fun loadCurrentUser(client: KeycloakClient, userName: String, realm: String) = client.userByName(
    userName, realm
).id
