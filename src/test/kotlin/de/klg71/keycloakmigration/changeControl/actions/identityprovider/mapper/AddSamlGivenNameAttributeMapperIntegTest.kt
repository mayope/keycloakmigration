package de.klg71.keycloakmigration.changeControl.actions.identityprovider.mapper

import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.identityprovider.AddIdentityProviderAction
import de.klg71.keycloakmigration.changeControl.actions.identityprovider.AddSamlGivenNameAttributeMapperAction
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.identityProviderByAlias
import de.klg71.keycloakmigration.keycloakapi.identityProviderMapperByName
import de.klg71.keycloakmigration.keycloakapi.model.SAML_ATTRIBUTE_GIVENNAME
import de.klg71.keycloakmigration.keycloakapi.model.SAML_USER_ATTRIBUTE_IDP_MAPPER
import org.assertj.core.api.Assertions
import org.junit.Test
import org.koin.core.component.inject

class AddSamlGivenNameAttributeMapperIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()

    @Test
    fun testAddSamlGivenNameAttributeMapper() {
        val identityProviderConfig = mapOf(
            "authorizationUrl" to "https://testUrl",
            "tokenUrl" to "https://testUrl",
            "issuer" to "issuer",
            "defaultScopes" to "scope1,scope2"
        )
        val identityProviderAlias = "test"
        AddIdentityProviderAction(
            testRealm, identityProviderAlias, "saml", identityProviderConfig, displayName = "displayName", true, true, true, true,
            "first broker login", ""
        ).executeIt()

        val createdIdentityProvider = client.identityProviderByAlias(identityProviderAlias, testRealm)

        val mapperName = "mapperName"
        val samlGivenNameAttribute = "givenName"
        AddSamlGivenNameAttributeMapperAction(
            testRealm,
            createdIdentityProvider.alias,
            mapperName,
            samlGivenNameAttribute
        ).executeIt()

        val createdMapper = client.identityProviderMapperByName(identityProviderAlias, mapperName, testRealm)

        Assertions.assertThat((createdMapper.identityProviderMapper)).isEqualTo(
            SAML_USER_ATTRIBUTE_IDP_MAPPER
        )
        Assertions.assertThat((createdMapper.config["user.attribute"] ?: error("test error"))).isEqualTo(
            samlGivenNameAttribute
        )
        Assertions.assertThat((createdMapper.config["attribute.name"] ?: error("test error"))).isEqualTo(
            SAML_ATTRIBUTE_GIVENNAME
        )

    }
}
