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

class DeleteIdentityProviderMapperIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testDeleteIdentityProviderMapper() {
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
        Assertions.assertThat((createdMapper.config["user.attribute"] ?: error("test error"))).isEqualTo(
            userAttributeName
        )

        DeleteIdentityProviderMapperAction(
            testRealm,
            createdMapper.identityProviderAlias,
            createdMapper.name
        ).executeIt()

        Assertions.assertThat(
            client.identityProviderMapperExistsByName(identityProviderAlias, mapperName, testRealm)
        ).isFalse
    }

    @Test
    fun testDeleteIdentityProviderMapper_Rollback() {
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
        Assertions.assertThat((createdMapper.config["user.attribute"] ?: error("test error"))).isEqualTo(
            userAttributeName
        )

        val action = DeleteIdentityProviderMapperAction(
            testRealm,
            createdMapper.identityProviderAlias,
            createdMapper.name
        )
        action.executeIt()
        Assertions.assertThat(
            client.identityProviderMapperExistsByName(identityProviderAlias, mapperName, testRealm)
        ).isFalse

        action.undoIt()

        val restoredMapper = client.identityProviderMapperByName(identityProviderAlias, mapperName, testRealm)

        Assertions.assertThat(restoredMapper.identityProviderAlias).isEqualTo(createdMapper.identityProviderAlias)
        Assertions.assertThat(restoredMapper.identityProviderMapper).isEqualTo(createdMapper.identityProviderMapper)
        Assertions.assertThat(restoredMapper.name).isEqualTo(createdMapper.name)
        Assertions.assertThat(restoredMapper.config).isEqualTo(createdMapper.config)

    }
}
