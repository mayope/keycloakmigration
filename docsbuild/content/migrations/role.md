---
author: klg71
layout: post
title:  "Role Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/role/
---
# Role Migrations
All migrations referring to the role resource.
## addRole
Add a role to keycloak, fails if the role already exists
### Parameter
- realm: String, optional
- name: String, not optional,
- clientId: String, optional, default=realmRole,
- description: String, optional, default=""
- attributes: Map< String,List< String>>, optional, default=empty
- composite: Boolean, optional, default=false, must be true if compositeChildRoles is specified
- clientRole: Boolean, optional, default=false,
- containerId: String, optional, default=not set
- compositeChildRoles: List<RoleSelector>, optional, default=not set

#### subclass RoleSelector
- name: String, mandatory
- clientId: String, optional

### Example
    id: add-role
    author: klg71
    changes:
    - addRole:
        realm: master
        name: test3
        attributes:
          role:
          - value1
          - value2

## updateRole
Update an existing realm- or client-role in keycloak.
### Parameters
- realm: String, optional
- name: String, not optional,
- clientId: String, optional, default=realmRole,
- description: String, optional, default=""
- attributes: Map< String,List< String>>, optional
- composite: Boolean, optional, must be true if compositeChildRoles is specified
- clientRole: Boolean, optional
- containerId: String, optional
- compositeChildRoles: List<RoleSelector>, optional

#### subclass RoleSelector
- name: String, mandatory
- clientId: String, optional

### Example
```yaml
    id: add-role
    author: klg71
    changes:
    - addRole:
        realm: master
        name: test3
    - updateRole:
        realm: master
        name: test3
        description: I am a test role
        attributes:
          role:
          - value1
          - value2
```
## deleteRole
Delete a role from keycloak, fails if the role does not exist
### Parameter
- realm: String, optional
- name: String, not optional,
- clientId: String, optional, default=realmRole
### Example
    id: delete-role
    author: klg71
    changes:
    - deleteRole:
        realm: master
        name: test4
        clientId: test
