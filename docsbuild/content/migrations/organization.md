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

## deleteOrganization
Deletes an existing organization, identified by its alias. Identity providers linked to the
organization are unlinked but not deleted.

What happens to members depends on how they joined:
- Unmanaged members (users added to the organization) only lose their membership.
- Managed members (users created by signing in through the organization's identity provider) are
  deleted from the realm by Keycloak together with the organization. To protect them, the action
  fails if the organization has managed members, unless `deleteManagedMembers` is set.

On rollback, the organization is recreated with its name, alias, redirectUrl, domains and
attributes. Its identity providers are linked again and its unmanaged members are added back.
Deleted managed members can't be restored.

### Parameters
- realm: String, optional,
- alias: String, not optional,
- deleteManagedMembers: Boolean, optional, default=false, also delete the organization if this
  deletes its managed members' users

### Example
```yaml
    id: delete-organization
    author: isaac.mercieca
    realm: test
    changes:
      - deleteOrganization:
          alias: organization-alias
```

To delete an organization that still has managed members (whose user accounts Keycloak removes
along with the organization), set `deleteManagedMembers: true`:
```yaml
    id: delete-organization-with-managed-members
    author: isaac.mercieca
    realm: test
    changes:
      - deleteOrganization:
          alias: organization-alias
          deleteManagedMembers: true
```

## linkIdentityProvider
Links an existing identity provider to an organization.

### Parameters
- realm: String, optional
- organizationAlias: String, not optional, alias of the organization
- identityProviderAlias: String, not optional, alias of the identity provider

### Example
```yaml
id: link-identity-provider-to-org
author: abigail.cortis
realm: test
changes:
- linkIdentityProvider:
    organizationAlias: my-organization
    identityProviderAlias: my-identity-provider
```
