---
title: Gradle Groovy DSL
type: docs
---
# Gradle Groovy DSL

## Filetree:
- build.gradle
- settings.gradle
- keycloak-changelog.yml
- 01_create_realm.yml

`build.gradle`
```gradle
plugins {
  id "de.klg71.keycloakmigrationplugin" version "x.x.x"
}
repositories { 
  jcenter()
}

task keycloakMigrateLocal(type: KeycloakMigrationTask) {
      group = "keycloak"
      description = "Migrate the keycloak instance"

      migrationFile = "migration/keycloak-changelog.yml"
      adminUser = "admin"
      adminPassword = "admin"
      baseUrl = "http://localhost:8080"
      realm = "master"
      waitForKeycloak = false
      waitForKeycloakTimeout = 0L // infinit wait time
      parameters = [USERNAME: "testUser", PASSWORD: "testPassword"]
    }
   
```
`settings.gradle`
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
