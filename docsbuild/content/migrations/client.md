---
author: klg71
layout: post
title:  "Client Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/client/
---
# Client Migrations
All migrations referring to the client resource.
## addSimpleClient
Simple command to add a client to keycloak, TODO: add more fields
### Parameter
- realm: String, optional
- clientId: String, not optional,
- enabled: Boolean, optional, default=true
- attributes: Map< String, String>, optional, default = empty
- protocol: String, optional, default="openid-connect"
- secret: String, optional
- publicClient: Boolean, optional, default=true
- redirectUris: List< String>, optional, default=empty
### Example
    id: add-simple-client
    author: klg71
    changes:
    - addSimpleClient:
        realm: master
        clientId: test

## deleteClient
Delete a client in keycloak
### Parameter
- realm: String, optional
- clientId: String, not optional,
### Example
    id: delete-client
    author: klg71
    changes:
    - deleteClient:
        realm: master
        clientId: test

## importClient
Imports a client using the json representation.

### Parameters
- realm: String, optional
- clientRepresentationJsonFilename: String, not optional
- relativeToFile: Boolean, optional, default=true

### Example
    id: import-client
    author: klg71
    changes:
    - importClient:
          realm: master
          clientRepresentationJsonFilename: client.json
          relativeToFile: true

## updateClient
Update a client

### Parameters
- realm: String, optional
- clientId: String, not optional
- name: String, optional, default=no change
- description: String, optional, default=no change
- enabled: Boolean, optional, default=no change
- attributes: Map<String, String>, optional, default=no change
- protocol: String, optional, default=no change
- redirectUris: List< String>, optional, default=no change
- bearerOnly: Boolean, optional, default=no change
- directAccessGrantEnabled: Boolean, optional, default=no change
- implicitFlowEnabled: Boolean, optional, default=no change
- standardFlowEnabled: Boolean, optional, default=no change
- adminUrl: String, optional, default=no change
- baseUrl: String, optional, default=no change
- rootUrl: String, optional, default=no change
- publicClient: Boolean, optional, default=no change
- serviceAccountsEnabled: Boolean, optional, default=no change
- webOrigins: List< String>, optional, default=no change
- fullScopeAllowed: Boolean, optional, default=no change

### Example
    id: update-client
    author: klg71
    changes:
    - updateClient:
        realm: master
        clientId: testClient
        redirectUris: 
            - http://localhost:8080
            - https://www.example.com
            
## assignRoleToClient
Assigns a realm- or client-role(if roleClientId is set) to a service account of a client.

### Parameters
- realm: String, optional
- clientId: String, not optional
- role: String, not optional
- roleClientId: String, optional, default = realmRole

### Example
    id: add-client-roles
    author: klg71
    realm: integ-test
    changes:
      - addSimpleClient:
          clientId: testClientRoles
      - updateClient:
          clientId: testClientRoles
          serviceAccountsEnabled: true
          publicClient: false
      - assignRoleToClient:
          clientId: testClientRoles
          role: query-users
          roleClientId: realm-management

## addRoleScopeMapping
Adds a realm- or client-role(if roleClientId is set) to the cope mappings of a client.

See https://www.keycloak.org/docs/latest/server_admin/#_role_scope_mappings

### Parameters
- realm: String, optional
- clientId: String, not optional
- role: String, not optional
- roleClientId: String, optional, default = realmRole

### Example
    id: add-client-role-mapping
    author: klg71
    realm: integ-test
    changes:
      - addSimpleClient:
          clientId: testClientRoleScopeMappings
      - addRole:
          name: scope-mapping-role
      - updateClient:
          clientId: testClientRoleScopeMappings
          fullScopeAllowed: false
      - addRoleScopeMapping:
          clientId: testClientRoleScopeMappings
          role: scope-mapping-role
      - addRoleScopeMapping:
          clientId: testClientRoleScopeMappings
          role: query-users
          roleClientId: realm-management

## addMapper
adds a full configurable clientmapper, throws error if client or realm doesn't exist or mapper with same name already exists

> Only use this action if you can't find a convenient method to add the mapper below

### Parameters
- realm: String, optional
- clientId: String, not optional
- name: String, not optional
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
- clientId: String, not optional
- name: String, not optional

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
- clientId: String, not optional
- name: String, not optional
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
- clientId: String, not optional
- name: String, not optional
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
- clientId: String, not optional
- name: String, not optional
- userAttribute: String, not optional
- addToIdToken: Boolean , optional, default = true,
- addToAccessToken: Boolean, optional, default = true,
- addToUserInfo: Boolean, optional, default = true,
- claimName: String?, optional, default = << name parameter>>
- multivalued: Boolean, optional, default = false,
- aggregateAttributeValues: Boolean, optional, default = true

### Example:
    id: add-client-mappers
    author: klg71
    realm: integ-test
    changes:
      - addSimpleClient:
          clientId: testMappers
      - addUserAttributeMapper:
          clientId: testMappers
          name: userAttribute
          userAttribute: testAttribute
          addToUserInfo: false
## addUserRealmRoleMapper
adds a user-realm-role clientmapper, throws error if client or realm doesn't exist or mapper with same name already exists

### Parameters
- realm: String, optional
- clientId: String, not optional
- name: String, not optional
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
