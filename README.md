# Keycloakmigration

This project is inspired by the database migration tool liquibase.
It aims to provide a similiar mechanism for Keycloak.

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


TODOS:
- Add more commands
- Add gradle plugin
- Add sophisticated unit and integration Tests
- Test Keycloak with PostgreSQL instead of H2 backend.
