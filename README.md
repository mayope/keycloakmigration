# Keycloakmigration

This project is inspired by the database migration tool liquibase.
It aims to provide a similiar mechanism for Keycloak. There also exists a gradle plugin for automated build pipelines: [keycloakmigrationplugin](https://github.com/klg71/keycloakmigrationplugin).

# Usage
Then migration can simply be invoked through the jar.

    java -jar keycloakmigration.jar

    usage: [-h] [-u USER] [-p PASSWORD] [-b BASEURL] [MIGRATION-FILE]

    optional arguments:
      -h, --help            show this help message and exit

      -u USER, --user USER  Username for the migration user, defaulting to admin.

      -p PASSWORD,          Password for the migration user, defaulting to admin.
      --password PASSWORD

      -b BASEURL,           Base url of keycloak server, defaulting to
      --baseurl BASEURL     http://localhost:18080/auth.


positional arguments:
  MIGRATION-FILE        File to migrate, defaulting to keycloak-changelog.yml

# Migration Details
Migrations are controlled through the changelog. It contains the changeSets used to execute the migration.

    
    includes:
      - path: changesets/01_initial.yml
      - path: changesets/02_second.yml

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
#### Descriptions
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
#### Descriptions
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
### addUserAttribute
### deleteUserAttribute
### assignRole
### revokeRole

## Group Migrations
### addGroup

## Role Migrations
### addRole
### deleteRole

## Client Migrations
### addSimpleClient
### deleteClient

## User Federation Migrations
### AddAdLdap

# TODOS:
- Add more commands
- Add gradle plugin
- Add sophisticated unit and integration Tests
- Test Keycloak with PostgreSQL instead of H2 backend.
