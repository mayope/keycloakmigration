---
title: Parameter Substitution
type: docs
---
# Migration Files
There are two types of files to control migrations in keycloak. ChangeLog and ChangeSet (may sound similar in liquibase).
The Changelog references all changeSets to apply and the ChangeSets contain the actual changes.


## Changelog
Migrations are controlled through the changelog. It contains the changeSets used to execute the migration.

#### Parameters
- includes: List of changesets to apply consisting of:
    - path: String, not optional, path to changeset
    - relativeToFile: Boolean, optional, default=true, whether the file should be searched from the working dir or relative to the keycloak changelog file.

#### Example
    includes:
      - path: 01_initial.yml
      - path: 02_second.yml
      - path: changes/03_third.yml
        relativeToFile: true


### ChangeSet
The changeSet contains the actual changes as a list of migrations (see [Supported Migrations]({{<ref "migrations" >}}))

#### Parameters
- id: String, not optional, describe the change
- author: String, not optional, author of the changeset
- realm: String, optional, preset realm for actions
> Please note that you must either provide a realm in the ChangeSet or in each action! Even though both parameters are optional one must be set!
- enabled: Boolean, optional, default=true, can be set through environment variable substitution to enable dynamic changesets see: https://github.com/klg71/keycloakmigration/issues/9

- changes: List of Migrations

#### Example
```yaml
id: initial-keycloak
author: klg71
realm: master
changes:
- addUser:
    name: test
    enabled: true
    emailVerified: true
    attributes:
      test:
      - test
      - test2
- updateUser:
    realm: otherRealm
    name: test
    enabled: false
    lastName: Lukas
```
        
## Parameter substitution
This format supports substitution of environment variables for dynamic content. The hash however will not include the resolved variable and just encode the file as a hash.
#### Example

```yaml
id: initial-keycloak
author: klg71
realm: master
changes:
- addUser:
    name: test
    enabled: true
    emailVerified: true
    attributes:
      test:
      - ${JAVA_HOME}
      - test2
```

This will replace `${JAVA_HOME}` with the system variable JAVA_HOME present at runtime

Parameter substitution is implemented through apache-common-text [StringSubstitutor](http://commons.apache.org/proper/commons-text/apidocs/org/apache/commons/text/StringSubstitutor.html).

You can also provide default values through the `${UNDEFINED_NUMBER:-1234}` (default value is after the `:-` separator).

You may also use the string interpolation features provided by StringSubstitutor but be careful to don't hurt the readability by doing so.

### Missing parameters
Missing parameter can lead to unexpected behaviour of the changelog parser and you may want to report them.

To fail on missing parameters you can use the option `failOnUndefinedVariables`(default = false) and to warn `warnOnUndefinedVariable`(default = true)
