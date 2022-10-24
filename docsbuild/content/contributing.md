---
title: Contributing
type: docs
---
## Contributing

To start developing on this project you can use the gradle tasks.

To start the local development keycloak you can just use the task ```startLocalKeycloak```

Tested with OpenJdk 12 and Keycloak 19.0.0

If you want to contribute to this project please visit the [issues](https://github.com/klg71/keycloakmigration/issues) page and maybe you can find something interesting :)

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
