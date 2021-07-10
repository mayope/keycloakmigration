---
author: haraldsegliens
layout: post
title:  "Required Actions"
date:   2021-07-08 12:22:20 +0200
permalink: /migrations/requiredactions/
---
# Required Actions
All migrations referring to the Required Actions resource.
## AddRequiredAction
Adds a required action.

### Parameters
- realm: String, optional
- providerId: String, mandatory
- alias: String, mandatory
- name: String, mandatory
- config: Map<String,String>, optional
- defaultAction: Boolean, optional
- enabled: Boolean, optional
- priority: Boolean, optional

### Example
```yaml
id: add-required-action
author: klg71
realm: integ-test
changes:
  - addRequiredAction:
      providerId: verify-email
      alias: VERIFY_EMAIL
      name: Verify Email
      defaultAction: true
      enabled: true
```
## UpdateRequiredAction
Updates a required action.
Only updates provided values.
For an update of the required action `alias` use the oldAlias as `alias` and the newAlias in `newAlias`.

### Parameters
- realm: String, optional
- providerId: String, optional, default = no update,
- alias: String, mandatory
- newAlias: String, optional, default = no update
- name: String, optional, default = no update
- config: Map<String,String>, optional, default = no update
- defaultAction: Boolean, optional, default = no update
- enabled: Boolean, optional, default = no update
- priority: Boolean, optional, default = no update

### Example
```yaml
id: add-required-action
author: klg71
realm: integ-test
changes:
  - addRequiredAction:
      providerId: verify-email
      alias: VERIFY_EMAIL
      name: Verify Email
      defaultAction: true
      enabled: true
  - updateRequiredAction:
      providerId: verify-email
      alias: VERIFY_EMAIL
      name: Verify Email 2
```
## DeleteRequiredAction
Deletes a required action, if one with this alias exists

### Parameters
- realm: String, optional
- alias: String, mandatory

### Example
```yaml
id: delete-flow
author: klg71
realm: integ-test
changes:
  - addRequiredAction:
      providerId: verify-email
      alias: VERIFY_EMAIL
      name: Verify Email
      defaultAction: true
      enabled: true
  - deleteRequiredAction:
      alias: VERIFY_EMAIL
```

