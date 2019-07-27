# Keycloakmigration

This projects aims to automate the configuration of the keycloak authentication provider.
It is inspired by the database migration tool liquibase.
The goal is to provide a similar mechanism for Keycloak. There also exists a gradle plugin for automated build pipelines: [keycloakmigrationplugin](https://github.com/klg71/keycloakmigrationplugin).

# Usage
Then migration can simply be invoked through the jar.

    java -jar keycloakmigration.jar --help

    usage: [-h] [-u USER] [-p PASSWORD] [-b BASEURL] [MIGRATION-FILE] [-r REALM]
           [-c CLIENT]

    optional arguments:
      -h, --help            show this help message and exit

      -u USER, --user USER  Username for the migration user, defaulting to admin.

      -p PASSWORD,          Password for the migration user, defaulting to admin.
      --password PASSWORD

      -b BASEURL,           Base url of keycloak server, defaulting to
      --baseurl BASEURL     http://localhost:18080/auth.

      -r REALM,             Realm to use for migration, defaulting to master
      --realm REALM

      -c CLIENT,            Client to use for migration, defaulting to master
      --client CLIENT


# Migration Details
Migrations are controlled through the changelog. It contains the changeSets used to execute the migration.

    
    includes:
      - path: 01_initial.yml
      - path: 02_second.yml
      - path: changes/03_third.yml
        relativeToFile: true

## Include Parameters
- path: String, not optional, path to changeset
- relativeToFile: Boolean, optional, default=true, whether the file should be searched from the working dir or relative to the keycloak changelog file.

A changeset may then look like this:

    id: initial-keycloak
    author: klg71
    changes:
    - addUser:
        realm: master
        name: test
        enabled: true
        emailVerified: true
        attributes:
          test:
          - test
          - test2
    - updateUser:
        realm: master
        name: test
        enabled: false
        lastName: Lukas

The migration hashes are stored in the attribute named 'migration' in the migration user.

There are no transactions in keycloak though if the rollback fails there might be a non deterministic state.
If it fails I would like to receive a bug report for this.

# Supported migrations
This are the currently implemented commands. I hope I can find the time to implement more of them.
## User Migrations
### addUser
Adds a user to keycloak. Fails if a user with that name already exists.
#### Parameters
- realm: String, not optional
- name: String, not optional
- enabled: Boolean,  default=true
- emailVerified: Boolean,  default=true
- attributes: Map with Layout Map<String, List<String>>, default=empty
#### Example
    id: test
    author: klg71
    changes:
    - addUser:
        realm: master
        name: test
        enabled: true
        emailVerified: true
        attributes:
          test:
          - test
          - test2

### deleteUser
Removes a user from keycloak. Fails if a user with that name does not exists.
#### Parameters
- realm: String, not optional
- name: String, not optional
#### Example
    id: test
    author: klg71
    changes:
    - deleteUser:
        realm: master
        name: test
### updateUser
Updates an exiting user in keycloak. Fails if no user with given name exists.
#### Parameters
- realm: String, not optional
- name: String, not optional
- enabled: Boolean, default=no change
- emailVerified: Boolean, default=no change
- access: String, default=no change
- notBefore: Long, default=no change
- totp: Boolean, default=no change
- disableableCredentialTypes: List<String>, default=no change
- requiredActions: List<String>, default=no change
- email: String, default=no change
- firstName: String, default=no change
- lastName: String, default=no change
#### Example
    id: test
    author: klg71
    changes:
    - updateUser:
        realm: master
        name: test
        enabled: false
        lastName: Lukas

### addUserAttribute
Adds an attribute to an existing user. Throws an error if the user does not exist.

User attributes can't be set deterministic with the updateUser action.
#### Parameters
- realm: String, not optional
- name: String, not optional
- attributeName: String, not optional
- attributeValues: List<String>, not optional
- override: Boolean, default=false

#### Example
    id: test
    author: klg71
    changes:
    - addUserAttribute:
        realm: master
        name: test
        attributeName: test1
        attributeValues:
        - value1
        - value2

### deleteUserAttribute
Deletes an attribute to an existing user. Throws an error if the user does not exist.
#### Parameters
- realm: String, not optional
- name: String, not optional
- attributeName: String, not optional
- failOnMissing: Boolean, default=true

#### Example
    id: test
    author: klg71
    changes:
    - deleteUserAttribute:
        realm: master
        name: test
        attributeName: test1

### assignRole
Assigns a role to the given user. Fails if the user or the role doesn't exist.
#### Parameters
- realm: String, not optional
- user: String, not optional
- role: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - assignRole:
        realm: master
        user: testUser
        role: testRole

### revokeRole
Revokes a role from the given user. Fails if the user or the role doesn't exist or the user does not have the role assigned.

#### Parameters
- realm: String, not optional
- user: String, not optional
- role: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - revokeRole:
        realm: master
        user: testUser
        role: testRole
        
### assignGroup
Assigns a group to the given user. Fails if the user or the group doesn't exist.
#### Parameters
- realm: String, not optional
- user: String, not optional
- group: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - assignGroup:
        realm: master
        user: testUser
        group: testGroup
        
### revokeGroup
Revokes a group from the given user. Fails if the user or the group doesn't exist or the user doesnt have the group assigned .
#### Parameters
- realm: String, not optional
- user: String, not optional
- group: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - revokeGroup:
        realm: master
        user: testUser
        group: testGroup

## Group Migrations
### addGroup
Adds a new group to keycloak. Fails if the group already exists.

#### Parameters
- realm: String, not optional
- name: String, not optional
- parent: String, default=empty

#### Example
    id: test
    author: klg71
    changes:
    - addGroup:
        realm: master
        name: testUser

### deleteGroup
Removes a group from keycloak. Fails if the group does not exist.

#### Parameters
- realm: String, not optional
- name: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - deleteGroup:
        realm: master
        name: testUser

### updateGroup
Updates a group from keycloak. Fails if the group does not exist.

#### Parameters
- realm: String, not optional
- name: String, not optional
- attributes: Map<String,List<String>>, optional, default=existing attributes
- realmRoles: List<String>, optional, default=existing realm roles
- clientRoles: Map<String,List<String>>, optional, default=existing client roles

#### Example
    id: test
    author: klg71
    changes:
      - updateGroup:
          realm: master
          name: child1
          attributes:
            lkz:
              - "1234"
              
### assignRoleToGroup
Assigns a role to a group in keycloak. Fails if the group or the role does not exist.

#### Parameters
- realm: String, not optional
- role: String, not optional
- group: String, not optional
- clientId: String, optional, default=realmRole

#### Example
    id: test
    author: klg71
    changes:
      - assignRoleToGroup:
          realm: integ-test
          role: parent
          group: test3
          
### revokeRoleFromGroup
Revokes a role from a group in keycloak. Fails if the group or the role does not exist or the role is not assigned to the group.

#### Parameters
- realm: String, not optional
- role: String, not optional
- group: String, not optional
- clientId: String, optional, default=realmRole

#### Example
    id: test
    author: klg71
    changes:
      - revokeRoleFromGroup:
          realm: integ-test
          group: parent
          role: test3

## Role Migrations
### addRole
Add a role to keycloak, fails if the role already exists
#### Parameter
- realm: String, not optional
- name: String, not optional,
- clientId: String, optional, default=realmRole,
- description: String, optional, default=""
- attributes: Map<String,List<String>>, optional, default=empty
- composite: Boolean, optional, default=false
- clientRole: Boolean, optional, default=false,
- containerId: String, optional, default=not set

#### Example
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
### deleteRole
Delete a role from keycloak, fails if the role does not exist
#### Parameter
- realm: String, not optional
- name: String, not optional,
- clientId: String, optional, default=realmRole
#### Example
    id: delete-role
    author: klg71
    changes:
    - deleteRole:
        realm: master
        name: test4
        clientId: test

## Client Migrations
### addSimpleClient
Simple command to add a client to keycloak, TODO: add more fields
#### Parameter
- realm: String, not optional
- clientId: String, not optional,
- enabled: Boolean, optional, default=true
- attributes: Map<String, String>, optional, default = empty
- protocol: String, optional, default="openid-connect"
- redirectUris: List<String>, optional, default=empty
#### Example
    id: add-simple-client
    author: klg71
    changes:
    - addSimpleClient:
        realm: master
        clientId: test

### deleteClient
Delete a client in keycloak
#### Parameter
- realm: String, not optional
- clientId: String, not optional,
#### Example
    id: delete-client
    author: klg71
    changes:
    - deleteClient:
        realm: master
        clientId: test

### importClient
Imports a client using the json representation.
- realm: String, not optional
- clientId: String, not optional,

#### Parameters
- realm: String, not optional
- clientRepresentationJsonFilename: String, not optional
- relativeToFile: Boolean, optional, default=true

#### Example
    id: import-client
    author: klg71
    changes:
    - importClient:
          realm: master
          clientRepresentationJsonFilename: client.json
          relativeTofile: true

## User Federation Migrations
### AddAdLdap

# Hacking
To start developing on this project you can use the gradle tasks.

To start the local development keycloak you can just use the task ```startLocalKeycloak```

# TODOS:
- Add more commands
- Add sophisticated unit and integration Tests
- Add token refresh logic
- add runtime parameters
