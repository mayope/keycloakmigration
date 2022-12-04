---
author: klg71
layout: post
title:  "User Federation Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/identityprovider/
---
# IdentityProvider Migrations
All migrations referring to the identityprovider resource.
## AddKeycloakIdentityProvider
Adds a keycloak identity provider.

### Parameters
- realm: String, optional
- alias: String, not optional
- authorizationUrl: String, not optional
- tokenUrl: String, not optional
- clientId: String, not optional
- clientSecret: String, not optional
- clientAuthMethod: String, optional, default = "client_secret_post"
- logoutUrl: String, optional, default = ""
- userInfoUrl: String, optional, default = ""
- issuer: String, optional, default = ""
- displayName: String, optional, default = ""
- defaultScopes: List< String>, optional, default = emptyList()
- validateSignature: String, optional, default = ""
- forwardParameters: List<String>, optional, default = emptyList()
- loginHint: Boolean, optional, default = false
- disableUserInfo: Boolean, optional, default = false
- hideOnLoginPage: Boolean, optional, default = false
- backchannelSupported: Boolean, optional, default = false
- syncMode: String, optional, default = "IMPORT"
- acceptsPromptsNoneForwardFromClient: String, optional, default = ""
- uiLocales: Boolean, optional, default = true
- guiOrder: String, optional, default = ""
- useJwksUrl: Boolean, optional, default = false
- enabled: Boolean, optional, default = true
- trustEmail: Boolean, optional, default = false
- storeToken: Boolean, optional, default = false
- linkOnly: Boolean, optional, default = false
- firstBrokerLoginFlowAlias: String, optional, default = "first broker login"
- postBrokerLoginFlowAlias: String, optional, default = ""
- updateProfileFirstLoginMode: String, optional, default = "on"

### Example
```yaml
id: add-keycloak-identity-provider
author: klg71
realm: integ-test
changes:
  - addKeycloakIdentityProvider:
      alias: testAlias
      authorizationUrl: https://authUrl
      tokenUrl: https://tokenUrl
      clientId: clientId
      clientSecret: clientSecret
      hideOnLoginPage: true
      backchannelSupported: true
      trustEmail: true
      storeToken: true
```
## UpdateKeycloakIdentityProvider
Updates given fields of an existing keycloak identity provider but leaves all not provided fields untouched.

### Parameters
- realm: String, optional
- alias: String, not optional
- authorizationUrl: String, optional
- tokenUrl: String, optional
- clientId: String, optional
- clientSecret: String, optional
- clientAuthMethod: String, optional
- logoutUrl: String, optional
- userInfoUrl: String, optional
- issuer: String, optional
- displayName: String, optional
- defaultScopes: List< String>, optional
- validateSignature: String, optional
- forwardParameters: List<String>, optional
- loginHint: Boolean, optional
- disableUserInfo: Boolean, optional
- hideOnLoginPage: Boolean, optional
- backchannelSupported: Boolean, optional
- syncMode: String, optional
- acceptsPromptsNoneForwardFromClient: String, optional
- uiLocales: Boolean, optional
- guiOrder: String, optional
- useJwksUrl: Boolean, optional
- enabled: Boolean, optional
- trustEmail: Boolean, optional
- storeToken: Boolean, optional
- linkOnly: Boolean, optional
- firstBrokerLoginFlowAlias: String, optional
- postBrokerLoginFlowAlias: String, optional
- updateProfileFirstLoginMode: String, optional

### Example
```yaml
id: update-keycloak-identity-provider
author: sideisra
realm: integ-test
changes:
  - updateKeycloakIdentityProvider:
      alias: testAlias
      clientId: newClientId
      storeToken: false
```
## DeleteIdentityProvider
Deletes an identity provider, if one with this alias exists

### Parameters
- realm: String, optional
- alias: String, not optional

### Example
```yaml
id: delete-identity-provider
author: klg71
realm: integ-test
changes:
  - addIdentityProvider:
      alias: testAlias1
      providerId: keycloak-oidc
      trustEmail: true
      storeToken: true
      config:
        hideOnLoginPage: true
        backchannelSupported: true
        authorizationUrl: https://authUrl
        tokenUrl: https://tokenUrl
        clientId: clientId
        clientSecret: clientSecret
  - deleteIdentityProvider:
      alias: testAlias1
```

## AddIdentityProvider
Adds a generic identity provider, exact configuration has to be reverse engineered through the keycloak web frontend.
If you need an identity provider please open an issue or file a pull request.

Specific IdentityProvider actions (e.g. for Keycloak) are listed below


### Parameters
- realm: String, optional
- alias: String, not optional
- providerId: String, not optional
- config: Map<String, String> not optional
  
  configuration properties include for example:
  - acceptsPromptNoneForwardFromClient
  - authorizationUrl
  - backchannelSupported
  - clientAuthMethod
  - clientId
  - clientSecret
  - defaultScope
  - disableUserInfo
  - forwardParameters
  - guiOrder
  - hideOnLoginPage
  - issuer
  - loginHint
  - logoutUrl
  - syncMode
  - tokenUrl
  - uiLocales
  - useJwksUrl
  - userInfoUrl
  - validateSignature

- displayName: String, optional, default=""
- enabled: Boolean, optional, default = true
- trustEmail: Boolean, optional, default = false
- storeToken: Boolean, optional, default = false
- linkOnly: Boolean, optional, default = false
- firstBrokerLoginFlowAlias: String, optional, default = "first broker login"
- postBrokerLoginFlowAlias: String, optional, default = ""

### Example
```yaml
id: add-identity-provider
author: klg71
realm: integ-test
changes:
  - addIdentityProvider:
      alias: testAlias1
      providerId: keycloak-oidc
      trustEmail: true
      storeToken: true
      config:
        hideOnLoginPage: true
        backchannelSupported: true
        authorizationUrl: https://authUrl
        tokenUrl: https://tokenUrl
        clientId: clientId
        clientSecret: clientSecret
```

## UpdateIdentityProvider
Updates given fields of an existing keycloak identity provider but leaves all not provided fields untouched.

Updates given fields of a generic identity provider but leaves all not provided fields untouched. The exact configuration has to be reverse engineered through the keycloak web frontend.
If you need an identity provider please open an issue or file a pull request.

Specific IdentityProvider actions (e.g. for Keycloak) are listed below

### Parameters
- realm: String, optional
- alias: String, not optional
- providerId: String, optional
- config: Map<String, String>, optional

  configuration properties include for example:
  - acceptsPromptNoneForwardFromClient
  - authorizationUrl
  - backchannelSupported
  - clientAuthMethod
  - clientId
  - clientSecret
  - defaultScope
  - disableUserInfo
  - forwardParameters
  - guiOrder
  - hideOnLoginPage
  - issuer
  - loginHint
  - logoutUrl
  - syncMode
  - tokenUrl
  - uiLocales
  - useJwksUrl
  - userInfoUrl
  - validateSignature

- displayName: String optional
- enabled: Boolean, optional
- trustEmail: Boolean, optional
- storeToken: Boolean, optional
- linkOnly: Boolean, optional
- firstBrokerLoginFlowAlias: String, optional
- postBrokerLoginFlowAlias: String, optional

### Example
```yaml
id: update-identity-provider
author: sideisra
realm: integ-test
changes:
  - updateIdentityProvider:
      alias: testAlias1
      storeToken: false
      config:
        clientId: newClientId
```

## AddIdentityProviderMapper
Adds a mapper to an identity provider.
This is a generic mapper action where you need to reverse-engineer the correct properties and values.
Throws an error if the IDP doesn't exist or if a mapper with this name already exists in this IDP.

### Parameters
- realm: String, optional
- name: String, not optional
- identityProviderAlias: String, not optional
- identityProviderMapper: String, not optional

  possible values vary with the type of the IDP (e.g. "keycloak-oidc" or "saml")
- config: Map<String, String>, not optional

  configuration properties depend on the IDP and mapper types

### Example
```yaml
id: add-saml-mapper
author: klg71
realm: integ-test
changes:
  - addIdentityProviderMapper:
      identityProviderAlias: idpAlias
      name: surnameMapper
      identityProviderMapper: saml-user-attribute-idp-mapper
      config:
        "attribute.name": http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname
        "user.attribute": lastName
```    

## AddSamlMapper
Adds a mapper to an identity provider of type "saml" (IDP property `providerId`), i.e. is basically the same as `AddIdentityProviderMapper` but on creation checks that the IDP has the correct type.
This is a generic mapper action where you need to reverse-engineer the correct properties and values.
Throws an error if the IDP doesn't exist, is not of type "saml" or if a mapper with this name already exists in this IDP.

### Parameters
- realm: String, optional
- name: String, not optional
- identityProviderAlias: String, not optional
- identityProviderMapper: String, not optional

  typical values for example:
  - saml-user-attribute-idp-mapper (map user attributes)
  - saml-role-idp-mapper (map user roles)
- config: Map<String, String>, not optional

  configuration properties depend on the mapper type and include for example:
  - attribute.name (refers to claims names in the identity schema; see below for examples)
  - attribute.value (which source value gets mapped to which target value)
  - role
  - syncMode
  - user.attribute (specify the name of the attribute in SAML)

### Example
```yaml
id: add-saml-mapper
author: klg71
realm: integ-test
changes:
  - addSamlMapper:
      identityProviderAlias: idpAlias
      name: surnameMapper
      identityProviderMapper: saml-user-attribute-idp-mapper
      config:
        "attribute.name": http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname
        "user.attribute": lastName
```    

Other SAML specific values can be found at the [org.keycloak.broker.saml.mappers.* sections](https://www.keycloak.org/docs-api/19.0.3/javadocs/constant-values.html) in the keycloak docs.

For more specific SAML mapper actions, see the following entries.

## AddSamlEmailAddressAttributeMapper
Adds a mapper for the email attribute from SAML.
Mapper type and some config properties are hardcoded.
Throws an error if the IDP doesn't exist, is not of type "saml" or if a mapper with this name already exists in this IDP.

### Parameters
- realm: String, optional
- name: String, not optional
- identityProviderAlias: String, not optional
- attributeName: String, not optional
  
  name of the attribute that holds the email address in SAML

  (hardcoded attribute name is http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress)

### Example
```yaml
id: add-saml-email-address-mapper
author: klg71
realm: integ-test
changes:
  - addSamlEmailAddressAttributeMapper:
      identityProviderAlias: idpAlias
      name: emailAddressMapper
      attributeName: email
```   

## AddSamlGivenNameAttributeMapper
Adds a mapper for the given name attribute from SAML.
Mapper type and some config properties are hardcoded.
Throws an error if the IDP doesn't exist, is not of type "saml" or if a mapper with this name already exists in this IDP.

### Parameters
- realm: String, optional
- name: String, not optional
- identityProviderAlias: String, not optional
- attributeName: String, not optional

  name of the attribute that holds the given name in SAML

  (hardcoded attribute name is http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname)

### Example
```yaml
id: add-saml-given-name-mapper
author: klg71
realm: integ-test
changes:
  - addSamlGivenNameAttributeMapper:
      identityProviderAlias: idpAlias
      name: givenNameMapper
      attributeName: firstName
```    

## AddSamlNameAttributeMapper
Adds a mapper for the name attribute from SAML.
Mapper type and some config properties are hardcoded.
Throws an error if the IDP doesn't exist, is not of type "saml" or if a mapper with this name already exists in this IDP.

### Parameters
- realm: String, optional
- name: String, not optional
- identityProviderAlias: String, not optional
- attributeName: String, not optional

  name of the attribute that holds the name in SAML

  (hardcoded attribute name is http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name)

### Example
```yaml
id: add-saml-name-mapper
author: klg71
realm: integ-test
changes:
  - addSamlNameAttributeMapper:
      identityProviderAlias: idpAlias
      name: nameMapper
      attributeName: fullName
```    

## AddSamlSurnameAttributeMapper
Adds a mapper for the surname attribute from SAML.
Mapper type and some config properties are hardcoded.
Throws an error if the IDP doesn't exist, is not of type "saml" or if a mapper with this name already exists in this IDP.

### Parameters
- realm: String, optional
- name: String, not optional
- identityProviderAlias: String, not optional
- attributeName: String, not optional

  name of the attribute that holds the surname in SAML

  (hardcoded attribute name is http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname)

### Example
```yaml
id: add-saml-surname-mapper
author: klg71
realm: integ-test
changes:
  - addSamlSurnameAttributeMapper:
      identityProviderAlias: idpAlias
      name: surnameMapper
      attributeName: lastName
```    

## AddSamlRoleMapper
Adds a mapper for one source role from SAML to target role in keycloak.
To map multiple roles, add multiple `addSamlRoleMapper` entries.
Mapper type and some config properties are hardcoded.
Throws an error if the IDP doesn't exist, is not of type "saml" or if a mapper with this name already exists in this IDP.

### Parameters
- realm: String, optional
- name: String, not optional
- identityProviderAlias: String, not optional
- attributeValue: String, not optional

  name of the source role in SAML

  (hardcoded attribute name is http://schemas.microsoft.com/ws/2008/06/identity/claims/role)
- role: String, not optional

  name of the target role in keycloak

### Example
```yaml
id: add-saml-role-mapper
author: klg71
realm: integ-test
changes:
  - addSamlRoleMapper:
      identityProviderAlias: idpAlias
      name: samlRoleAToKeycloakRoleBMapper
      attributeValue: sourceRoleA
      role: targetRoleB
```    

## DeleteIdentityProviderMapper
Deletes a mapper from an identity provider.

### Parameters
- realm: String, optional
- name: String, not optional
- identityProviderAlias: String, not optional

### Example
```yaml
id: delete-identity-provider-mapper
author: klg71
realm: integ-test
changes:
  - deleteIdentityProviderMapper:
      identityProviderAlias: idpAlias
      name: surnameMapper
```    

