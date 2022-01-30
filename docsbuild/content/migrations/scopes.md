---
author: klg71
layout: post
title:  "ClientScope Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/scope/
---
# ClientScope Migrations
All migrations referring to the ClientScope resource.

## addClientScope
Adds a clientScope to keycloak, fails if a clientScope with that name already exists
### Parameters
- realm: String, optional
- name: String, not optional
- description: String, optional, default = null
- protocol: String, optional, default = "openid-connect"
- protocolMappers: List< ProtocolMapper >, optional, default = empty list
- consentScreenText: String, optional, default = null
- displayOnConsentScreen: Boolean, optional, default = false
- guiOrder: Int, optional, default = null
- includeInTokenScope: Boolean, optional, default = true

#### subclass ProtocolMapper
- name: String, mandatory
- protocol: String, mandatory, default = null
- protocolMapper: String, mandatory
- consentRequired: Boolean, optional
- config: Map<String, String>, optional, default = empty

### Example
```yaml
id: add-client-scopes
author: klg71
realm: integ-test
changes:
  - addClientScope:
      name: api
```

## assignRoleToClientScope
Adds a realm- or client-role (if roleClientId is set) to a clientScope.

### Parameters
- realm: String, optional
- name: String, not optional
- role: String, not optional
- roleClientId: String, optional, default = realmRole

### Example
```yaml
id: add-role-to-clientscope
author: klg71
realm: integ-test
changes:
 - addClientScope:
      name: testClientScope
 - addSimpleClient:
      clientId: testClient
 - addRole:
      name: test-role
      clientId: testClientScope
 - assignRoleToClientScope:
      name: testClientScope
      role: test-role
      roleClientId: testClient
```

## assignDefaultClientScope
Assigns a default clientScope to a client, fails if the client or scope doesn't exist.
### Parameters
- realm: String, optional
- clientScopeName: String
- clientId: String

### Example
```yaml
id: add-client-scopes
author: klg71
realm: integ-test
changes:
  - addSimpleClient:
      clientId: testClientScope
  - addClientScope:
      name: api
  - assignDefaultClientScope:
      clientId: testClientScope
      clientScopeName: api
```


## addClientScopeMapper
adds a full configurable client scope mapper, throws error if client or realm doesn't exist or mapper with same name already exists

> Only use this action if you can't find a convenient method to add the mapper below

### Parameters
- realm: String, optional
- clientScopeName: String, not optional
- name: String, not optional
- config: Map<String,String>, not optional
- protocolMapper: String, not optional
- protocol: String, optional, default="openid-connect"

### Example:
```yaml
    id: add-client-scope-mappers
    author: klg71
    realm: integ-test
    changes:
      - addClientScope:
          name: testMappers
      - addClientScopeMapper:
          clientScopeName: testMappers
          name: testPropertyMapper
          protocolMapper: oidc-usermodel-property-mapper
          config:
            access.token.claim: true
            id.token.claim: false
            userinfo.token.claim: false
            claim.name: customPropertyMapper
            jsonType.label: String
            user.attribute: UserModel.getEmail()
```
## deleteClientScopeMapper
deletes a client scope mapper

### Parameters
- realm: String, optional
- clientScopeName: String, not optional
- name: String, not optional

### Example:
```yaml
    id: add-client-scope-mappers
    author: klg71
    realm: integ-test
    changes:
      - addClientScope:
          name: testMappers
      - addClientScopeMapper:
          clientScopeName: testMappers
          name: testPropertyMapper
          protocolMapper: oidc-usermodel-property-mapper
          config:
            access.token.claim: true
            id.token.claim: false
            userinfo.token.claim: false
            claim.name: customPropertyMapper
            jsonType.label: String
            user.attribute: UserModel.getEmail()
      - deleteClientScopeMapper:
          clientScopeName: testMappers
          name: testPropertyMapper
```

## addClientScopeAudienceMapper
adds an audience client scope mapper, throws error if client or realm doesn't exist or mapper with same name already exists

### Parameters
- realm: String, optional
- clientScopeName: String, not optional
- name: String, not optional
- addToIdToken: Boolean , optional, default = true,
- addToAccessToken: Boolean, optional, default = true,
- clientAudience: String, optional, default = "",
- customAudience: String, optional, default = ""

### Example:
```yaml
    id: add-client-scope-mappers
    author: klg71
    realm: integ-test
    changes:
      - addClientScope:
          name: testMappers
      - addClientScopeAudienceMapper:
          clientScopeName: testMappers
          name: audienceMapper
          addToIdToken: false
          clientAudience: testMappers
          customAudience: completlyCustom
```

## addClientScopeGroupMembershipMapper
adds a group-membership client scope mapper, throws error if client or realm doesn't exist or mapper with same name already exists

### Parameters
- realm: String, optional
- clientScopeName: String, not optional
- name: String, not optional
- addToIdToken: Boolean , optional, default = true,
- addToAccessToken: Boolean, optional, default = true,
- addToUserInfo: Boolean, optional, default = true,
- fullGroupPath: Boolean, optional, default = true,
- claimName: String?, optional, default = << name parameter>>

### Example:
```yaml
    id: add-client-scope-mappers
    author: klg71
    realm: integ-test
    changes:
      - addClientScope:
          name: testMappers
      - addClientScopeGroupMembershipMapper:
          clientScopeName: testMappers
          name: groupMembership
          addToAccessToken: false
          claimName: groupClaim
```

## addClientScopeUserAttributeMapper
adds a user-attribute client scope mapper, throws error if client or realm doesn't exist or mapper with same name already exists

### Parameters
- realm: String, optional
- clientScopeName: String, not optional
- name: String, not optional
- userAttribute: String, not optional
- addToIdToken: Boolean , optional, default = true,
- addToAccessToken: Boolean, optional, default = true,
- addToUserInfo: Boolean, optional, default = true,
- claimName: String?, optional, default = << name parameter>>
- multivalued: Boolean, optional, default = false,
- aggregateAttributeValues: Boolean, optional, default = true

### Example:
```yaml
    id: add-client-scope-mappers
    author: klg71
    realm: integ-test
    changes:
      - addClientScope:
          name: testMappers
      - addClientScopeUserAttributeMapper:
          clientScopeName: testMappers
          name: userAttribute
          userAttribute: testAttribute
          addToUserInfo: false
```

## addClientScopeUserRealmRoleMapper
adds a user-realm-role client scope mapper, throws error if client or realm doesn't exist or mapper with same name already exists

### Parameters
- realm: String, optional
- clientScopeName: String, not optional
- name: String, not optional
- addToIdToken: Boolean , optional, default = true,
- addToAccessToken: Boolean, optional, default = true,
- addToUserInfo: Boolean, optional, default = true,
- claimName: String?, optional, default = << name parameter>>
- prefix: String, optional, default = ""

### Example:
```yaml
    id: add-client-scope-mappers
    author: klg71
    realm: integ-test
    changes:
      - addClientScope:
          name: testMappers
      - addClientScopeUserRealmRoleMapper:
          clientScopeName: testMappers
          name: userRealmRole
          prefix: rolePrefix
```