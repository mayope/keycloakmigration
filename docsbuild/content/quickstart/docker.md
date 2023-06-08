---
title: Usage with Docker
type: docs
---

# Usage with docker

you can also use keycloakmigraiton with docker. The migrations are then controlled trough environment variables and mounted volumes.

## Usage
```shell script
docker run --env BASEURL=http://localhost:8080 klg71/keycloakmigration 
```

## Environment variables
- `KEYCLOAK_CHANGELOG` migration changelog, should be mounted 
   
   default: /migration/keycloak-changelog.yml 
 
- `ADMIN_USER` user to execute migration

   default: admin
   
- `ADMIN_PASSWORD` password of user to execute migration

   default: admin

- `ADMIN_TOTP` totp of user to execute migration

   default: unset

- `ADMIN_CLIENT` client to use for admin login

   default: admin-cli
- `LOGIN_REALM` realm to use for admin login

   default: master
- `BASEURL` Baseurl for the keycloak instance

   default: http://localhost:8080/auth
- `WAIT_FOR_KEYCLOAK`(flag) If keycloakmigration should wait for the keycloak instance

   default: unset
- `WAIT_FOR_KEYCLOAK_TIMEOUT` Wait time till keycloak migration fails if keycloak is not ready (only if `WAIT_FOR_KEYCLOAK` is set)

   default:   0(infinit) 
- `FAIL_ON_UNDEFINED_VARIABLES`(flag) If keycloakmigration should fail if undefined template variables occur

   default:   unset=false
- `DISABLE_WARN_ON_UNDEFINED_VARIABLES`(flag) If keycloakmigration should not warn if undefined template variables occur

   default:   unset=false
- `CORRECT_HASHES`(flag) If keycloakmigration should migrate not matching hashes (see [Hash Troubleshooting]({{< relref "/hashmigration.md" >}}) )

   default:   unset=false
- `STAY_IDLE`(flag) Don't kill container when migration is finished (useful for k8s deployment)

   default:   unset=false
