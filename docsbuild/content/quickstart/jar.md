---
title: Usage trough fatjar
type: docs
---

# Usage through fatjar

## Filetree:
- keycloakmigration-x.x.x-fat.jar (download on [github releases](https://github.com/klg71/keycloakmigration/releases))
- keycloak-changelog.yml
- 01_create_realm.yml

## Execute
```shell script
java -jar keycloakmigration-x.x.x-fat.jar
```
## Arguments

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
Available migrations are listed here: [Migrations]({{<ref "/migrations/" >}})

