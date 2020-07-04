---
title: Using the Keycloak Api
type: docs
---
# Using the Keycloak Api
For the migrations to work I had to implement a keycloak administration api.

This api provides a KeycloakClient which connects to the keycloak instance.
This client has methods to manage user, group, role, client, realms and userfederation resources.

To use it you can simply the maven dependency or download the keycloakapi-jar from the releases page.
## Gradle Groovy DSL
```groovy
// https://mvnrepository.com/artifact/de.klg71.keycloakmigration/keycloakmigration
compile group: 'de.klg71.keycloakmigration', name: 'keycloakmapi', version: 'x.x.x'
```
## Gradle Kotlin DSL
```groovy
// https://mvnrepository.com/artifact/de.klg71.keycloakmigration/keycloakmigration
compile("de.klg71.keycloakmigration:keycloakapi:x.x.x")
```
## Maven
```xml
<!-- https://mvnrepository.com/artifact/de.klg71.keycloakmigration/keycloakmigration -->
<dependency>
    <groupId>de.klg71.keycloakmigration</groupId>
    <artifactId>keycloakapi</artifactId>
    <version>x.x.x</version>
</dependency>
```

The keycloakapi uses [OpenFeign](https://github.com/OpenFeign/feign) to connect to the keycloak instance.

Full documentation is available here: [Documentation](/keycloakmigration/documentation/keycloakapi)
