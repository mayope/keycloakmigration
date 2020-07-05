---
author: klg71
layout: post
title:  "ClientScope Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/scope/
---
# ClientScope Migrations
All migrations referring to the ClientScope resource.

## addClientScope
Adds a clientScope to keycloak, fails if a clientScope with that name already exists
### Parameter
- realm: String, optional
- name: String, not optional
- description: String, optional, default = null
- protocol: String, optional, default = "openid-connect"
- consentScreenText: String, optional, default = null
- displayOnConsentScreen: Boolean, optional, default = false
- guiOrder: Int, optional, default = null
- includeInTokenScope: Boolean, optional, default = true

### Example
```yaml
id: add-client-scopes
author: klg71
realm: integ-test
changes:
  - addClientScope:
      name: api
```

## assignDefaultClientScope
Assigns a default clientScope to a client, fails if the client or scope doesn't exist.
### Parameter
- realm: String, optional
- clientScopeName: String
- clientId: String

### Example
```yaml
id: add-client-scopes
author: klg71
realm: integ-test
changes:
  - addSimpleClient:
      clientId: testClientScope
  - addClientScope:
      name: api
  - assignDefaultClientScope:
      clientId: testClientScope
      clientScopeName: api
```
