---
author: haraldsegliens
layout: post
title:  "Required Actions"
date:   2021-07-08 12:22:20 +0200
permalink: /migrations/requiredactions/
---
# Required Actions
All migrations referring to the Required Actions resource in Authentication.

## RegisterRequiredAction
Registers a required action

### Parameters
- realm: String, optional
- name: String, mandatory
- providerId: String, mandatory

### Example
```yaml
id: register-required-action
author: klg71
realm: integ-test
changes:
  - registerRequiredAction:
      providerId: verify-email
      name: Verify Email
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
  - registerRequiredAction:
      providerId: verify-email
      name: Verify Email
  - updateRequiredAction:
      providerId: verify-email
      alias: VERIFY_EMAIL
      name: Verify Email 2
```

