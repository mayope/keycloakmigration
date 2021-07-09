---
author: janunld
layout: post
title:  "ProtocolMapper Migrations"
date:   2020-07-09 20:03:42 +0200
permalink: /migrations/mappers/
---
# ProtocolMapper Migrations
All migrations referring to the ProtocolMapper resource.

## addMapper
Adds a fully configurable protocol mapper to either a client, or a client scope, throws an error if client, client scope
or realm doesn't exist or mapper with same name already exists

> Only use this action if you can't find a convenient method to add the mapper below

### Parameters
- realm: String, optional
- name: String, not optional
- clientId: String, optional if client scope is set
- clientScopeName: String, optional if client id is set
- config: Map<String,String>, not optional
- protocolMapper: String, not optional
- protocol: String, optional, default="openid-connect"

### Example:
    id: add-client-mappers
    author: klg71
    realm: integ-test
    changes:
      - addSimpleClient:
          clientId: testMappers
      - addMapper:
          clientId: testMappers
          name: testPropertyMapper
          protocolMapper: oidc-usermodel-property-mapper
          config:
            access.token.claim: true
            id.token.claim: false
            userinfo.token.claim: false
            claim.name: customPropertyMapper
            jsonType.label: String
            user.attribute: UserModel.getEmail()

## deleteMapper
deletes a client mapper

### Parameters
- realm: String, optional
- name: String, not optional
- clientId: String, optional if client scope is set
- clientScopeName: String, optional if client id is set

### Example:
    id: add-client-mappers
    author: klg71
    realm: integ-test
    changes:
      - addSimpleClient:
          clientId: testMappers
      - addMapper:
          clientId: testMappers
          name: testPropertyMapper
          protocolMapper: oidc-usermodel-property-mapper
          config:
            access.token.claim: true
            id.token.claim: false
            userinfo.token.claim: false
            claim.name: customPropertyMapper
            jsonType.label: String
            user.attribute: UserModel.getEmail()
      - deleteMapper:
          clientId: testMappers
          name: testPropertyMapper

## addAudienceMapper
adds a audience clientmapper, throws error if client or realm doesn't exist or mapper with same name already exists

### Parameters
- realm: String, optional
- name: String, not optional
- clientId: String, optional if client scope is set
- clientScopeName: String, optional if client id is set
- addToIdToken: Boolean , optional, default = true,
- addToAccessToken: Boolean, optional, default = true,
- clientAudience: String, optional, default = "",
- customAudience: String, optional, default = ""

### Example:
    id: add-client-mappers
    author: klg71
    realm: integ-test
    changes:
      - addSimpleClient:
          clientId: testMappers
      - addAudienceMapper:
          clientId: testMappers
          name: audienceMapper
          addToIdToken: false
          clientAudience: testMappers
          customAudience: completlyCustom
## addGroupMembershipMapper
adds a group-membership clientmapper, throws error if client or realm doesn't exist or mapper with same name already exists

### Parameters
- realm: String, optional
- name: String, not optional
- clientId: String, optional if client scope is set
- clientScopeName: String, optional if client id is set
- addToIdToken: Boolean , optional, default = true,
- addToAccessToken: Boolean, optional, default = true,
- addToUserInfo: Boolean, optional, default = true,
- fullGroupPath: Boolean, optional, default = true,
- claimName: String?, optional, default = << name parameter>>

### Example:
    id: add-client-mappers
    author: klg71
    realm: integ-test
    changes:
      - addSimpleClient:
          clientId: testMappers
      - addGroupMembershipMapper:
          clientId: testMappers
          name: groupMembership
          addToAccessToken: false
          claimName: groupClaim
## addUserAttributeMapper
adds a user-attribute clientmapper, throws error if client or realm doesn't exist or mapper with same name already exists

### Parameters
- realm: String, optional
- name: String, not optional
- clientId: String, optional if client scope is set
- clientScopeName: String, optional if client id is set
- userAttribute: String, not optional
- addToIdToken: Boolean , optional, default = true,
- addToAccessToken: Boolean, optional, default = true,
- addToUserInfo: Boolean, optional, default = true,
- claimName: String?, optional, default = << name parameter>>
- multivalued: Boolean, optional, default = false,
- aggregateAttributeValues: Boolean, optional, default = true

### Example:
    id: add-scope-mapper
    author: klg71
    realm: integ-test
    changes:
      - addClientScope:
          name: testMappers
      - addUserAttributeMapper:
          clientScopeName: testMappers
          name: userAttribute
          userAttribute: testAttribute
          addToUserInfo: false
## addUserRealmRoleMapper
adds a user-realm-role clientmapper, throws error if client or realm doesn't exist or mapper with same name already exists

### Parameters
- realm: String, optional
- name: String, not optional
- clientId: String, optional if client scope is set
- clientScopeName: String, optional if client id is set
- addToIdToken: Boolean , optional, default = true,
- addToAccessToken: Boolean, optional, default = true,
- addToUserInfo: Boolean, optional, default = true,
- claimName: String?, optional, default = << name parameter>>
- prefix: String, optional, default = ""

### Example:
    id: add-client-mappers
    author: klg71
    realm: integ-test
    changes:
      - addSimpleClient:
          clientId: testMappers
      - addUserRealmRoleMapper:
          clientId: testMappers
          name: userRealmRole
          prefix: rolePrefix

