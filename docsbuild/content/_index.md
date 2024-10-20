---
title: Keycloakmigration
type: docs
---

# Keycloakmigration
Infrastructure as code for the [Keycloak](https://www.keycloak.org/) authentication provider 

This projects aims to automate the configuration of the keycloak authentication provider.
It is inspired by the database migration tool [liquibase](https://www.liquibase.org/).
The goal is to provide a similar mechanism for Keycloak. 

## Usage

### Gradle
Apply the current version of the Plugin:

```gradle
plugins {
  id "de.klg71.keycloakmigrationplugin" version "x.x.x"
}
```

Use the Tasks to execute the migration:
#### Groovy
```groovy

    task keycloakMigrateLocal(type: KeycloakMigrationTask) {
      group = "keycloak"
      description = "Migrate the keycloak instance"

      migrationFile = "migration/keycloak-changelog.yml"
      adminUser = "admin"
      adminPassword = "admin"
      baseUrl = "http://localhost:8080"
      realm = "master"
      parameters = [USERNAME: "testUser", PASSWORD: "testPassword"]
      waitForKeycloak = false
      waitForKeycloakTimeout = 0L // infinit wait time
      failOnUndefinedVariables = false
      warnOnUndefinedVariables = true
    }
```
### Kotlin
```kotlin
    register<KeycloakMigrationTask>("keycloakMigrateLocal") {
        group = "keycloak"
        description = "Migrate the keycloak instance"

        migrationFile = "migration/keycloak-changelog.yml"
        adminUser = "admin"
        adminPassword = "admin"
        baseUrl = "http://localhost:8080/auth"
        realm = "master"
        parameters = mapOf(
                "USER_NAME" to "testUser",
                "PASSWORD" to "password"
        )
        waitForKeycloak = false
        waitForKeycloakTimeout = 0L // infinit wait time
        failOnUndefinedVariables = false
        warnOnUndefinedVariables = true
    }
```
   To correct existing hashes please use the `KeycloakMigrationCorrectHashesTask`.
   
### Using the fatjar
Then migration can simply be invoked through the jar. (Found attached to the latest release on github https://github.com/klg71/keycloakmigration/releases)

    java -jar keycloakmigration.jar --help
    usage: [-h] [-u USER] [-p PASSWORD] [-t TOTP] [-o] [-P]... [-b BASEURL]
    [MIGRATION-FILE] [-r REALM] [-c CLIENT] [--correct-hashes]
    [-k PARAMETER]... [--wait-for-keycloak]
    [--wait-for-keycloak-timeout WAIT_FOR_KEYCLOAK_TIMEOUT]
    [--fail-on-undefined-variables] [--disable-warn-on-undefined-variables]

    optional arguments:
    -h, --help                                              show this help message
                                                            and exit

    -u USER, --user USER                                    Username for the
                                                            migration user,
                                                            defaulting to admin.
  
    -p PASSWORD, --password PASSWORD                        Password for the
                                                            migration user,
                                                            defaulting to admin.
  
    -t TOTP, --totp TOTP                                    Time based one time
                                                            password for the
                                                            migration user,
                                                            empty per default
  
    -o, --use-oauth                                         Use OAuth2 for login
                                                            instead of
                                                            user/pass/(totp),
                                                            defaulting to false.
  
    -P, --use-oauth-local-port                              Which port to listen
                                                            for the auth code
                                                            callback, defaulting
                                                            to 8081.
  
    -b BASEURL, --baseurl BASEURL                           Base url of keycloak
                                                            server, defaulting
                                                            to
                                                            http://localhost:80
                                                            80/auth.
  
    -r REALM, --realm REALM                                 Realm to use for
                                                            migration,
                                                            defaulting to master
  
    -c CLIENT, --client CLIENT                              Client to use for
                                                            migration,
                                                            defaulting to
                                                            admin-cli
  
    --correct-hashes                                        Correct hashes to
                                                            most recent version,
                                                            defaulting to false
  
                                                            Just choose this
                                                            option if you didn't
                                                            change anything in
                                                            the changelog since
                                                            the last migration!
                                                            This will replace
                                                            all old hashes with
                                                            the new hash version
                                                            and can be omitted
                                                            next time the
                                                            migration is run.
                                                            See README.md for
                                                            further explanation!
  
    -k PARAMETER, --parameter PARAMETER                     Parameters to
                                                            substitute in
                                                            changelog, syntax
                                                            is: -k param1=value1
                                                            will replace
                                                            ${param1} with
                                                            value1 in changelog
  
    --wait-for-keycloak                                     Wait for Keycloak to
                                                            become ready,
                                                            defaulting to false.
  
    --wait-for-keycloak-timeout WAIT_FOR_KEYCLOAK_TIMEOUT   Wait for Keycloak to
                                                            become ready timeout
                                                            in seconds
                                                            (defaulting to
                                                            0=infinit).
  
    --fail-on-undefined-variables                           Fail if variables
                                                            could not be
                                                            replaced, defaulting
                                                            to false.
  
    --disable-warn-on-undefined-variables                   Disables warning if
                                                            variables could not
                                                            be replaced,
                                                            defaulting to false.


    positional arguments:
      MIGRATION-FILE                                        File to migrate
                                                            defaulting to 
                                                            keycloak-changelog.yml
