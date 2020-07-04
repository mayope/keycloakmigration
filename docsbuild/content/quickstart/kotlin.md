---
title: Gradle Kotlin DSL
type: docs
---
# Gradle Kotlin DSL

## Filetree:
- build.gradle.kts
- settings.gradle.kts
- keycloak-changelog.yml
- 01_create_realm.yml

`build.gradle.kts`
```gradle
plugins {
  id("de.klg71.keycloakmigrationplugin") version "x.x.x"
}
repositories { 
  jcenter()
}

tasks {
    register<KeycloakMigrationTask>("keycloakMigrateLocal") {
      migrationFile = "keycloak-changelog.yml"
      adminUser = "admin"
      adminPassword = "adminPassword"
      baseUrl = "http://localhost:8080"
      realm = "master"
      waitForKeycloak(keycloakBaseUrl)
      parameters = mapOf(
              "USER_NAME" to "testUser",
              "PASSWORD" to "password"
      )
      waitForKeycloak = false
      waitForKeycloakTimeout = 0L // infinit wait time
    }
}
```
`settings.gradle.kts`
```gradle
rootProject.name = "keycloak-migration"
```

`keycloak-changelog.yml`
```yaml
includes:
  - path: 01_create_realm.yml
```
`01_create_realm.yml`
```yaml
id: create-realm
author: lmeisege
changes:
  - addRealm:
      name: kubernetes
  - updateRealm:
      id: kubernetes
      rememberMe: true
      ssoSessionIdleTimeout: 36000
```

## Execute
```shell script
gradle keycloakMigrateLocal
```
Available migrations are listed here: [Migrations]({{<ref "/migrations/" >}})

