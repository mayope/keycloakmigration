# Keycloakmigration ![Maven metadata URL](https://img.shields.io/maven-metadata/v/https/repo.maven.apache.org/maven2/de/klg71/keycloakmigration/keycloakmigration/maven-metadata.xml.svg?label=mavenCentral)

This projects aims to automate the configuration of the keycloak authentication provider.
It is inspired by the database migration tool liquibase.
The goal is to provide a similar mechanism for Keycloak. 

## Documentation
For full documentation please visit: https://mayope.github.io/keycloakmigration/

## Quickstart
### Gradle
Apply the current version of the Plugin:

```gradle
plugins {
  id "de.klg71.keycloakmigrationplugin" version "x.x.x"
}
```

Use the Tasks to execute the migration:
#### Groovy
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
    
### Kotlin
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
    
   To correct existing hashes please use the `KeycloakMigrationCorrectHashesTask`.
   
### Using the fatjar
Then migration can simply be invoked through the jar. (Found attached to the latest release on github https://github.com/klg71/keycloakmigration/releases)

    java -jar keycloakmigration.jar --help
    usage: [-h] [-u USER] [-p PASSWORD] [-b BASEURL] [MIGRATION-FILE] [-r REALM]
           [-c CLIENT] [--correct-hashes] [-k PARAMETER]... [--wait-for-keycloak]
           [--wait-for-keycloak-timeout WAIT_FOR_KEYCLOAK_TIMEOUT]

    optional arguments:
      -h, --help                                              show this help message and exit

      -u USER, --user USER                                    Username for the migration user, defaulting to admin.

      -p PASSWORD, --password PASSWORD                        Password for the migration user, defaulting to admin.

      -b BASEURL, --baseurl BASEURL                           Base url of keycloak server, defaulting to
                                                              http://localhost:18080/auth.
                                                              
      -r REALM, --realm REALM                                 Realm to use for migration, defaulting to master

      -c CLIENT, --client CLIENT                              Client to use for
                                                              migration,
                                                              defaulting to master

      --correct-hashes                                        Correct hashes to most recent version, defaulting to false

                                                              Just choose this option if you didn't change anything in
                                                              the changelog since the last migration!
                                                              This will replace all old hashes with the new hash version
                                                              and can be omitted next time the migration is run.
                                                              See README.md for further explanation!

      -k PARAMETER, --parameter PARAMETER                     Parameters to substitute in changelog, syntax is:
                                                              -k param1=value1
                                                              will replace
                                                              ${param1} with
                                                              value1 in changelog

      --wait-for-keycloak                                     Wait for Keycloak to become ready.

      --wait-for-keycloak-timeout WAIT_FOR_KEYCLOAK_TIMEOUT   Wait for Keycloak to become ready timeout in seconds (defaulting to 0=infinit).
      
      --fail-on-undefined-variables                           Fail if variables could not be replaced, defaulting to false.
  
      --warn-on-undefined-variables                           Fail if variables could not bereplaced, to true.                                                
      

    positional arguments:
      MIGRATION-FILE                                          File to migrate, defaulting to keycloak-changelog.yml
      


## TODOS
- Add commands for identity providers (see issues)
- batch updates for users
