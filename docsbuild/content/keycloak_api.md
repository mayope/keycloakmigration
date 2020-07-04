---
title: Using the Keycloak Api
type: docs
---
# Using the Keycloak Api
For the migrations to work I had to implement a keycloak administration api.

This api provides a KeycloakClient which connects to the keycloak instance.
This client has methods to manage user, group, role, client, realms and userfederation resources.

To use it you can simply the maven dependency or download the keycloakapi-jar from the releases page.
