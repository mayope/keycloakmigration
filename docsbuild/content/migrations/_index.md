---
layout: post
title:  "Available Actions"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/
---

## Supported migrations
Currently we implemented migration actions for the following resources:
- [User](usermigrations.markdown) 
- [Group](groupmigrations.markdown) 
- [Role](rolemigrations.markdown) 
- [Realm](realmmigrations.markdown) 
- [Client](clientmigrations.markdown) 
- [UserFederation](userfederationmigrations.markdown) 

For more examples see `src/test/resources/changesets`.


## Technical Hints

The migration hashes are stored in the attribute named 'migration' in the migration user.

There are no transactions in keycloak though if the rollback fails there might be a non deterministic state!
If it fails I would like to receive a bug report for this.

If you struggle with invalid hashes on a linux-windows setup check the line endings of the json-import files as git might check them out as LF where on windows its CRLF and thus producing the error in hashing.

If you are using git you can place the following file into the dir with the json-import-files to reassure that the line ending is always LF.

`.gitattributes`:

    ** text eol=lf

## Hacking
To start developing on this project you can use the gradle tasks.

To start the local development keycloak you can just use the task ```startLocalKeycloak```

Tested with OpenJdk 12 and Keycloak 19.0.0

### Use keycloakmigration through maven dependency
#### Gradle dependency
     // https://mvnrepository.com/artifact/de.klg71.keycloakmigration/keycloakmigration
     compile group: 'de.klg71.keycloakmigration', name: 'keycloakmigration', version: 'x.x.x'
 ### Usage
 Kotlin
 ```kotlin
    class MyMigrationArgs(private val adminUser: String,
                          private val adminPassword: String,
                          private val migrationFile: String,
                          private val baseUrl: String,
                          private val realm: String,
                          private val clientId: String,
                          private val correctHashes: Boolean,
                          private val parameters: Map<String, String>) : MigrationArgs {
        override fun adminUser() = adminUser
        override fun adminPassword() = adminPassword
        override fun baseUrl() = baseUrl
        override fun migrationFile() = migrationFile
        override fun realm() = realm
        override fun clientId() = clientId
        override fun correctHashes() = correctHashes
        override fun parameters() = parameters
    }

    class KeycloakMigrationExecution  {
        fun migrate() {
            MyMigrationArgs("admin", "adminPass", "keycloak-changelog.yml",
                            "https://myauthserver", "master",
                            "admin-cli", false, emptyMap())
                    .let {
                        de.klg71.keycloakmigration.migrate(it)
                    }
        }

    }
```
## TODOS
- Add more commands
- Add sophisticated unit and integration Tests
- batch updates for users
