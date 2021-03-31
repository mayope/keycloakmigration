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
- clientAuthMethod: String = "client_secret_post",
- logoutUrl: String, optional default = "",
- userInfoUrl: String, optional default = "",
- issuer: String, optional default = "",
- displayName: String, optional default = "",
- defaultScopes: List< String>, optional default = emptyList(),
- validateSignature: String, optional default = "",
- forwardParameters: List<String>, optional default = emptyList(),
- loginHint: Boolean, optional default = false,
- disableUserInfo: Boolean, optional default = false,
- hideOnLoginPage: Boolean, optional default = false,
- backchannelSupported: Boolean, optional default = false,
- syncMode: String, optional default = "IMPORT",
- acceptsPromptsNoneForwardFromClient: String, optional default = "",
- uiLocales: Boolean, optional default = true,
- guiOrder: String, optional default = "",
- useJwksUrl: Boolean, optional default = false,
- enabled: Boolean, optional default = true,
- trustEmail: Boolean, optional default = false,
- storeToken: Boolean, optional default = false,
- linkOnly: Boolean, optional default = false,
- firstBrokerLoginFlowAlias: String, optional default = "first broker login",
- postBrokerLoginFlowAlias: String, optional default = "",
- updateProfileFirstLoginMode: String, optional default = "on",

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
Adds a generic identity provider, exact configuration has to be reverse engineered through the keycloak web frontent.
If you need an identity provider please open an issue or file a pull request.

Specific IdentityProvider actions (e.g. for Keycloak) are listed below


### Parameters
- realm: String, optional
- alias: String, not optional
- providerId: String,
- config: Map<String, String> not optional,
  
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
  
- displayName: String optional, default=",
- enabled: Boolean, optional, default = true,
- trustEmail: Boolean, optional, default = false,
- storeToken: Boolean, optional, default = false,
- linkOnly: Boolean, optional, default = false,
- firstBrokerLoginFlowAlias: String, optional, default = "first broker login",
- postBrokerLoginFlowAlias: String, optional, default = "",

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
                
