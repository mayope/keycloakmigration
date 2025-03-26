---
author: isaac.mercieca
layout: post
title:  "Organization Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/organization/
---
# Organization Migrations
All migrations referring to the organization resource.

## addOrganization
Adds a new organization to an exsisting realm

### Parameters
- realm: String, optional
- name: String, not optional,
- alias: String, optional, default=name
- redirectUrl: String, optional,
- domains: List<OrganizationDomain>

### Example
```yaml
    id: add-organization
    author: isaac.mercieca
    changes:
    - addOrganization:
        name: test
        domains:
          - name: test.com
```

#### OrganizationDomain
##### Parameters
- name: String,
- verified: Boolean, optional, default=false