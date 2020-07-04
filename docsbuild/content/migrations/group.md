---
author: klg71
layout: post
title:  "Group Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/group/
---
# Group Migrations
All migrations referring to the group resource.
## addGroup
Adds a new group to keycloak. Fails if the group already exists.

### Parameters
- realm: String, optional
- name: String, not optional
- parent: String, default=empty

### Example
    id: test
    author: klg71
    changes:
    - addGroup:
        realm: master
        name: testGroup

## deleteGroup
Removes a group from keycloak. Fails if the group does not exist.

### Parameters
- realm: String, optional
- name: String, not optional

### Example
    id: test
    author: klg71
    changes:
    - deleteGroup:
        realm: master
        name: testUser

## updateGroup
Updates a group from keycloak. Fails if the group does not exist.

### Parameters
- realm: String, optional
- name: String, not optional
- attributes: Map< String,List< String>>, optional, default=existing attributes
- realmRoles: List< String>, optional, default=existing realm roles
- clientRoles: Map< String,List< String>>, optional, default=existing client roles, Key of the map is the clientId and the value is a List of roleNames to attach

### Example
    id: test
    author: klg71
    changes:
      - updateGroup:
          realm: master
          name: child1
          attributes:
            lkz:
              - "1234"
              
## assignRoleToGroup
Assigns a role to a group in keycloak. Fails if the group or the role does not exist.

### Parameters
- realm: String, optional
- role: String, not optional
- group: String, not optional
- clientId: String, optional, default=realmRole

### Example
    id: test
    author: klg71
    changes:
      - assignRoleToGroup:
          realm: integ-test
          role: parent
          group: test3
          
## revokeRoleFromGroup
Revokes a role from a group in keycloak. Fails if the group or the role does not exist or the role is not assigned to the group.

### Parameters
- realm: String, optional
- role: String, not optional
- group: String, not optional
- clientId: String, optional, default=realmRole

### Example
    id: test
    author: klg71
    changes:
      - revokeRoleFromGroup:
          realm: integ-test
          group: parent
          role: test3
