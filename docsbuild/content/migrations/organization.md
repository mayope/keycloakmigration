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
Adds a new organization to an existing realm

### Parameters
- realm: String, optional
- name: String, not optional,
- alias: String, optional, default=name
- redirectUrl: String, optional,
- domains: List<OrganizationDomain>,
- attributes: Map with Layout Map<String, List< String >>, default=empty

### Example
```yaml
    id: add-organization
    author: isaac.mercieca
    changes:
    - addOrganization:
        name: test
        domains:
          - name: test.com
        attributes:
          custom-attribute: 
            - attributeValue
```

#### OrganizationDomain
##### Parameters
- name: String,
- verified: Boolean, optional, default=false

## updateOrganization
Updates an existing organization. The alias may not be updated.

### Parameters
- realm: String, optional,
- alias: String, not optional,
- name: String, optional,
- redirectUrl: String, optional,
- domains: List<OrganizationDomain>, optional,
- attributes: Map with Layout Map<String, List< String >>, default=empty

### Example
```yaml
    id: update-organization
    author: david.briffa
    realm: test
    changes:
    - updateOrganization:
        alias: organization-alias
        name: updated-name
        redirectUrl: http://redirectUrl.com
        domains:
          - name: updated-domain.com
        attributes:
          custom-attribute: 
            - updated-value
```

#### OrganizationDomain
##### Parameters
- name: String,
- verified: Boolean, optional, default=false