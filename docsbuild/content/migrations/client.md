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
