package de.klg71.keycloakmigration.changeControl.actions.identityprovider.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.identityprovider.AddIdentityProviderAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.identityProviderByAlias
import de.klg71.keycloakmigration.keycloakapi.identityProviderMapperByName
import de.klg71.keycloakmigration.keycloakapi.identityProviderMapperExistsByName
import org.assertj.core.api.Assertions
import org.junit.Test
import org.koin.core.component.inject

class AddIdentityProviderMapperIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddIdentityProviderMapper() {
        val identityProviderConfig = mapOf(
            "authorizationUrl" to "https://testUrl",
            "tokenUrl" to "https://testUrl",
            "issuer" to "issuer",
            "defaultScopes" to "scope1,scope2"
        )
        val identityProviderAlias = "test"
        AddIdentityProviderAction(
            testRealm, identityProviderAlias, "saml", identityProviderConfig, displayName = "displayName", true, true,
            true, true,
            "first broker login", ""
        ).executeIt()

        val createdIdentityProvider = client.identityProviderByAlias(identityProviderAlias, testRealm)

        val mapperName = "mapperName"
        val mapperType = "saml-user-attribute-idp-mapper"
        val attributeName = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"
        val userAttributeName = "attributeNameToBeMappedFrom"
        val config = mapOf(
            "user.attribute" to userAttributeName,
            "attribute.name" to attributeName
        )
        AddIdentityProviderMapperAction(
            testRealm,
            config,
            createdIdentityProvider.alias,
            mapperType,
            mapperName
        ).executeIt()

        val createdMapper = client.identityProviderMapperByName(identityProviderAlias, mapperName, testRealm)

        Assertions.assertThat((createdMapper.identityProviderMapper)).isEqualTo(
            mapperType
        )
        Assertions.assertThat((createdMapper.config["user.attribute"] ?: error("test error"))).isEqualTo(
            userAttributeName
        )
        Assertions.assertThat((createdMapper.config["attribute.name"] ?: error("test error"))).isEqualTo(
            attributeName
        )

    }

    @Test
    fun testAddIdentityProviderMapper_Rollback() {
        val identityProviderConfig = mapOf(
            "authorizationUrl" to "https://testUrl",
            "tokenUrl" to "https://testUrl",
            "issuer" to "issuer",
            "defaultScopes" to "scope1,scope2"
        )
        val identityProviderAlias = "test"
        AddIdentityProviderAction(
            testRealm, identityProviderAlias, "saml", identityProviderConfig, displayName = "displayName", true, true,
            true, true,
            "first broker login", ""
        ).executeIt()

        val createdIdentityProvider = client.identityProviderByAlias(identityProviderAlias, testRealm)

        val mapperName = "mapperName"
        val mapperType = "saml-user-attribute-idp-mapper"
        val attributeName = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"
        val userAttributeName = "attributeNameToBeMappedFrom"
        val config = mapOf(
            "user.attribute" to userAttributeName,
            "attribute.name" to attributeName
        )
        val action = AddIdentityProviderMapperAction(
            testRealm,
            config,
            createdIdentityProvider.alias,
            mapperType,
            mapperName
        )
        action.executeIt()
        val createdMapper = client.identityProviderMapperByName(identityProviderAlias, mapperName, testRealm)
        Assertions.assertThat((createdMapper.config["user.attribute"] ?: error("test error"))).isEqualTo(
            userAttributeName
        )

        action.undoIt()

        Assertions.assertThat(
            client.identityProviderMapperExistsByName(identityProviderAlias, mapperName, testRealm)
        ).isFalse
    }
}
