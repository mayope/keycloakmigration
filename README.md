# Keycloakmigration![Maven metadata URL](https://img.shields.io/maven-metadata/v/https/repo.maven.apache.org/maven2/de/klg71/keycloakmigration/keycloakmigration/maven-metadata.xml.svg?label=mavenCentral)

This projects aims to automate the configuration of the keycloak authentication provider.
It is inspired by the database migration tool liquibase.
The goal is to provide a similar mechanism for Keycloak. There also exists a gradle plugin for automated build pipelines: [keycloakmigrationplugin](https://github.com/klg71/keycloakmigrationplugin).

# Usage
Then migration can simply be invoked through the jar.

    java -jar keycloakmigration.jar --help
    usage: [-h] [-u USER] [-p PASSWORD] [-b BASEURL] [MIGRATION-FILE] [-r REALM]
           [-c CLIENT] [--correct-hashes]

    optional arguments:
      -h, --help            show this help message and exit

      -u USER, --user USER  Username for the migration user, defaulting to admin.

      -p PASSWORD,          Password for the migration user, defaulting to admin.
      --password PASSWORD

      -b BASEURL,           Base url of keycloak server, defaulting to
      --baseurl BASEURL     http://localhost:18080/auth.

      -r REALM,             Realm to use for migration, defaulting to master
      --realm REALM

      -c CLIENT,            Client to use for migration, defaulting to master
      --client CLIENT

      --correct-hashes      Correct hashes to most recent version, defaulting to
                            false!
                            Just choose this option if you didn't change
                            anything in the changelog since the last migration!
                            This will replace all old hashes with the new hash
                            version and can be omitted next time the migration is
                            run. See README.md for further explanation!


    positional arguments:
      MIGRATION-FILE        File to migrate, defaulting to keycloak-changelog.yml

# Migration Files
There are two types of files to control migrations in keycloak. ChangeLog and ChangeSet (may sound similar in liquibase).
The Changelog references all changeSets to apply and the ChangeSets contain the actual changes.



## Changelog
Migrations are controlled through the changelog. It contains the changeSets used to execute the migration.

### Parameters
- includes: List of changesets to apply consisting of:
    - path: String, not optional, path to changeset
    - relativeToFile: Boolean, optional, default=true, whether the file should be searched from the working dir or relative to the keycloak changelog file.

### Example
    includes:
      - path: 01_initial.yml
      - path: 02_second.yml
      - path: changes/03_third.yml
        relativeToFile: true


## ChangeSet
The changeSet contains the actual changes as a list of migrations (see [Supported Migrations](#supported-migrations))

### Parameters
- id: String, not optional, describe the change
- author: String, not optional, author of the changeset
- realm: String, optional, preset realm for actions
> Please note that you must either provide a realm in the ChangeSet or in each action! Even though both parameters are optional one must be set!

- changes: List of Migrations

### Example

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
        
# Parameter substitution
This format supports substitution of environment variables for dynamic content. The hash however will not include the resolved variable and just encode the file as a hash.
### Example

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

This will replace `${JAVA_HOME}` with the system variable JAVA_HOME present at runtime


# Troubleshooting hashes
The hash implementation from 0.0.12 to 0.1.0 has changed so the old hashes will always throw an error.
An hash error may also occur if you did make a syntactic but not semantic change to the changelog.
You can however call the migration script with the `--correct-hashes` switch and it will just replace the failing hashes.
This will skip any control mechanism and you must to make sure that you have the same changelog that you migrated before.
It will only check for the number of hashes to skip or execute migrations!

> **Dont use the `--correct-hashes` switch in build pipelines!**

# Supported migrations
This are the currently implemented commands. I hope I can find the time to implement more of them.
## User Migrations
### addUser
Adds a user to keycloak. Fails if a user with that name already exists.
#### Parameters
- realm: String, optional
- name: String, not optional
- enabled: Boolean,  default=true
- emailVerified: Boolean,  default=true
- attributes: Map with Layout Map<String, List<String>>, default=empty
#### Example
    id: test
    author: klg71
    changes:
    - addUser:
        realm: master
        name: test
        enabled: true
        emailVerified: true
        attributes:
          test:
          - test
          - test2

### deleteUser
Removes a user from keycloak. Fails if a user with that name does not exists.
#### Parameters
- realm: String, optional
- name: String, not optional
#### Example
    id: test
    author: klg71
    changes:
    - deleteUser:
        realm: master
        name: test
### updateUser
Updates an exiting user in keycloak. Fails if no user with given name exists.
#### Parameters
- realm: String, optional
- name: String, not optional
- enabled: Boolean, default=no change
- emailVerified: Boolean, default=no change
- access: String, default=no change
- notBefore: Long, default=no change
- totp: Boolean, default=no change
- disableableCredentialTypes: List<String>, default=no change
- requiredActions: List<String>, default=no change
- email: String, default=no change
- firstName: String, default=no change
- lastName: String, default=no change
#### Example
    id: test
    author: klg71
    changes:
    - updateUser:
        realm: master
        name: test
        enabled: false
        lastName: Lukas

### addUserAttribute
Adds an attribute to an existing user. Throws an error if the user does not exist.

User attributes can't be set deterministic with the updateUser action.
#### Parameters
- realm: String, optional
- name: String, not optional
- attributeName: String, not optional
- attributeValues: List<String>, not optional
- override: Boolean, default=false

#### Example
    id: test
    author: klg71
    changes:
    - addUserAttribute:
        realm: master
        name: test
        attributeName: test1
        attributeValues:
        - value1
        - value2

### deleteUserAttribute
Deletes an attribute to an existing user. Throws an error if the user does not exist.
#### Parameters
- realm: String, optional
- name: String, not optional
- attributeName: String, not optional
- failOnMissing: Boolean, default=true

#### Example
    id: test
    author: klg71
    changes:
    - deleteUserAttribute:
        realm: master
        name: test
        attributeName: test1

### assignRole
Assigns a role to the given user. Fails if the user or the role doesn't exist.
#### Parameters
- realm: String, optional
- user: String, not optional
- role: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - assignRole:
        realm: master
        user: testUser
        role: testRole

### revokeRole
Revokes a role from the given user. Fails if the user or the role doesn't exist or the user does not have the role assigned.

#### Parameters
- realm: String, optional
- user: String, not optional
- role: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - revokeRole:
        realm: master
        user: testUser
        role: testRole
        
### assignGroup
Assigns a group to the given user. Fails if the user or the group doesn't exist.
#### Parameters
- realm: String, optional
- user: String, not optional
- group: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - assignGroup:
        realm: master
        user: testUser
        group: testGroup
        
### revokeGroup
Revokes a group from the given user. Fails if the user or the group doesn't exist or the user doesnt have the group assigned .
#### Parameters
- realm: String, optional
- user: String, not optional
- group: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - revokeGroup:
        realm: master
        user: testUser
        group: testGroup

## Group Migrations
### addGroup
Adds a new group to keycloak. Fails if the group already exists.

#### Parameters
- realm: String, optional
- name: String, not optional
- parent: String, default=empty

#### Example
    id: test
    author: klg71
    changes:
    - addGroup:
        realm: master
        name: testUser

### deleteGroup
Removes a group from keycloak. Fails if the group does not exist.

#### Parameters
- realm: String, optional
- name: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - deleteGroup:
        realm: master
        name: testUser

### updateGroup
Updates a group from keycloak. Fails if the group does not exist.

#### Parameters
- realm: String, optional
- name: String, not optional
- attributes: Map<String,List<String>>, optional, default=existing attributes
- realmRoles: List<String>, optional, default=existing realm roles
- clientRoles: Map<String,List<String>>, optional, default=existing client roles

#### Example
    id: test
    author: klg71
    changes:
      - updateGroup:
          realm: master
          name: child1
          attributes:
            lkz:
              - "1234"
              
### assignRoleToGroup
Assigns a role to a group in keycloak. Fails if the group or the role does not exist.

#### Parameters
- realm: String, optional
- role: String, not optional
- group: String, not optional
- clientId: String, optional, default=realmRole

#### Example
    id: test
    author: klg71
    changes:
      - assignRoleToGroup:
          realm: integ-test
          role: parent
          group: test3
          
### revokeRoleFromGroup
Revokes a role from a group in keycloak. Fails if the group or the role does not exist or the role is not assigned to the group.

#### Parameters
- realm: String, optional
- role: String, not optional
- group: String, not optional
- clientId: String, optional, default=realmRole

#### Example
    id: test
    author: klg71
    changes:
      - revokeRoleFromGroup:
          realm: integ-test
          group: parent
          role: test3

## Role Migrations
### addRole
Add a role to keycloak, fails if the role already exists
#### Parameter
- realm: String, optional
- name: String, not optional,
- clientId: String, optional, default=realmRole,
- description: String, optional, default=""
- attributes: Map<String,List<String>>, optional, default=empty
- composite: Boolean, optional, default=false
- clientRole: Boolean, optional, default=false,
- containerId: String, optional, default=not set

#### Example
    id: add-role
    author: klg71
    changes:
    - addRole:
        realm: master
        name: test3
        attributes:
          role:
          - value1
          - value2
### deleteRole
Delete a role from keycloak, fails if the role does not exist
#### Parameter
- realm: String, optional
- name: String, not optional,
- clientId: String, optional, default=realmRole
#### Example
    id: delete-role
    author: klg71
    changes:
    - deleteRole:
        realm: master
        name: test4
        clientId: test

## Client Migrations
### addSimpleClient
Simple command to add a client to keycloak, TODO: add more fields
#### Parameter
- realm: String, optional
- clientId: String, not optional,
- enabled: Boolean, optional, default=true
- attributes: Map<String, String>, optional, default = empty
- protocol: String, optional, default="openid-connect"
- redirectUris: List<String>, optional, default=empty
#### Example
    id: add-simple-client
    author: klg71
    changes:
    - addSimpleClient:
        realm: master
        clientId: test

### deleteClient
Delete a client in keycloak
#### Parameter
- realm: String, optional
- clientId: String, not optional,
#### Example
    id: delete-client
    author: klg71
    changes:
    - deleteClient:
        realm: master
        clientId: test

### importClient
Imports a client using the json representation.

#### Parameters
- realm: String, optional
- clientRepresentationJsonFilename: String, not optional
- relativeToFile: Boolean, optional, default=true

#### Example
    id: import-client
    author: klg71
    changes:
    - importClient:
          realm: master
          clientRepresentationJsonFilename: client.json
          relativeTofile: true

### updateClient
Update a client

#### Parameters
- realm: String, optional
- clientId: String, not optional
- name: String, optional, default=no change
- description: String, optional, default=no change
- enabled: Boolean, optional, default=no change
- attributes: Map<String, String>, optional, default=no change
- protocol: String, optional, default=no change
- redirectUris: List<String>, optional, default=no change
- bearerOnly: Boolean, optional, default=no change
- directAccessGrantEnabled: Boolean, optional, default=no change
- implicitFlowEnabled: Boolean, optional, default=no change
- standardFlowEnabled: Boolean, optional, default=no change
- adminUrl: String, optional, default=no change
- baseUrl: String, optional, default=no change
- rootUrl: String, optional, default=no change

#### Example
    id: update-client
    author: klg71
    changes:
    - updateClient:
        realm: master
        clientId: testClient
        redirectUris: 
            - http://localhost:8080
            - https://www.example.com
            
            
## Realm Migrations

### addRealm
adds a Realm, throws error if realm with that id already exists

#### Parameters
- name: String, not optional
- enabled: Boolean, optional, default=true
- id: String, optional, default=name

#### Example
    id: add-realm
    author: klg71
    changes:
      - addRealm:
          name: integ-test
          
### deleteRealm
deletes a Realm, throws error if realm with that id does not exists

#### Parameters
- id: String, not optional

#### Example
    id: add-realm
    author: klg71
    changes:
      - deleteRealm:
          id: integ-test
          
### updateRealm
updates a Realm, throws error if realm with that id does not exists

#### Parameters
- id: String, not optional
- realmName: String, optional
- displayName:String, optional
- displayNameHtml:String, optional
- revokeRefreshToken:Boolean, optional
- refreshTokenMaxReuse:Int, optional
- accessTokenLifespan:Int, optional
- accessTokenLifespanForImplicitFlow:Int, optional
- ssoSessionIdleTimeout:Int, optional
- ssoSessionMaxLifespan:Int, optional
- ssoSessionIdleTimeoutRememberMe:Int, optional
- ssoSessionMaxLifespanRememberMe:Int, optional
- offlineSessionIdleTimeout:Int, optional
- offlineSessionMaxLifespanEnabled:Boolean, optional
- offlineSessionMaxLifespan:Int, optional
- accessCodeLifespan:Int, optional
- accessCodeLifespanUserAction:Int, optional
- accessCodeLifespanLogin:Int, optional
- actionTokenGeneratedByAdminLifespan:Int, optional
- actionTokenGeneratedByUserLifespan:Int, optional
- enabled:Boolean, optional
- sslRequired:String, optional
- registrationAllowed:Boolean, optional
- registrationEmailAsUsername:Boolean, optional
- rememberMe:Boolean, optional
- verifyEmail:Boolean, optional
- loginWithEmailAllowed:Boolean, optional
- duplicateEmailsAllowed:Boolean, optional
- resetPasswordAllowed:Boolean, optional
- editUsernameAllowed:Boolean, optional
- bruteForceProtected:Boolean, optional
- permanentLockout:Boolean, optional
- maxFailureWaitSeconds:Int, optional
- minimumQuickLoginWaitSeconds:Int, optional
- waitIncrementSeconds:Int, optional
- quickLoginCheckMilliSeconds:Int, optional
- maxDeltaTimeSeconds:Int, optional
- failureFactor:Int, optional
- defaultRoles:List<String>, optional
- requiredCredentials:List<String>, optional
- otpPolicyType:String, optional
- otpPolicyAlgorithm:String, optional
- otpPolicyInitialCounter:Int, optional
- otpPolicyDigits:Int, optional
- otpPolicyLookAheadWindow:Int, optional
- otpPolicyPeriod:Int, optional
- otpSupportedApplications:List<String>, optional
- webAuthnPolicyRpEntityName:String, optional
- webAuthnPolicySignatureAlgorithms:List<String>, optional
- webAuthnPolicyRpId:String, optional
- webAuthnPolicyAttestationConveyancePreference:String, optional
- webAuthnPolicyAuthenticatorAttachment:String, optional
- webAuthnPolicyRequireResidentKey:String, optional
- webAuthnPolicyUserVerificationRequirement:String, optional
- webAuthnPolicyCreateTimeout:Int, optional
- webAuthnPolicyAvoidSameAuthenticatorRegister:Boolean, optional
- webAuthnPolicyAcceptableAaguids:List<String>,
- browserSecurityHeaders:Map<String,String>, optional
- smtpServer:Map<String,String>, optional
- eventsEnabled:Boolean, optional
- eventsListeners:List<String>, optional
- enabledEventTypes:List<String>, optional
- adminEventsEnabled:Boolean, optional
- adminEventsDetailsEnabled:Boolean, optional
- internationalizationEnabled:Boolean, optional
- supportedLocales:List<String>, optional
- browserFlow:String, optional
- registrationFlow:String, optional
- directGrantFlow:String, optional
- resetCredentialsFlow:String, optional
- clientAuthenticationFlow:String, optional
- dockerAuthenticationFlow:String, optional
- attributes:Map<String,String>, optional (Map gets merged if attributes are not present in yaml). Following keys are supported in keycloak 8.0.1:
    - webAuthnPolicyAuthenticatorAttachment
    - _browser_header.xRobotsTag
    - webAuthnPolicyRpEntityName
    - failureFactor
    - actionTokenGeneratedByUserLifespan
    - maxDeltaTimeSeconds
    - webAuthnPolicySignatureAlgorithms
    - frontendUrl
    - offlineSessionMaxLifespan
    - _browser_header.contentSecurityPolicyReportOnly
    - bruteForceProtected
    - _browser_header.contentSecurityPolicy
    - _browser_header.xXSSProtection
    - _browser_header.xFrameOptions
    - _browser_header.strictTransportSecurity
    - webAuthnPolicyUserVerificationRequirement
    - permanentLockout
    - quickLoginCheckMilliSeconds
    - webAuthnPolicyCreateTimeout
    - webAuthnPolicyRequireResidentKey
    - webAuthnPolicyRpId
    - webAuthnPolicyAttestationConveyancePreference
    - maxFailureWaitSeconds
    - minimumQuickLoginWaitSeconds
    - webAuthnPolicyAvoidSameAuthenticatorRegister
    - _browser_header.xContentTypeOptions
    - actionTokenGeneratedByAdminLifespan
    - waitIncrementSeconds
    - offlineSessionMaxLifespanEnabled
- userManagedAccessAllowed:Boolean, optional

#### Example
    id: update-realm
    author: klg71
    changes:
      - updateRealm:
          id: integ-test
          displayName: UpdatedRealm
          

## User Federation Migrations
### AddAdLdap
Adds an active directory to the realm

#### Parameters
- realm: String, optional
- name: String, not optional
- config: Map<String, String> values:
    - connectionUrl, not optional
    - usersDn, not optional
    - bindCredential, not optional
    - bindDn, not optional
    - changedSyncPeriod, optional, default:"86400"
    - fullSyncPeriod, optional, default:"604800"
    - useKerberosAuthentication, optional, default: "false"
    - allowKerberosAuthentication, optional, default: "false"
    - pagination, optional, default: "true"
    - readTimeout, optional, default: ""
    - connectionTimeout, optional, default: ""
    - connectionPooling, optional, default: "true"
    - useTruststoreSPI, optional, default: "ldapsOnly"
    - validatePasswordPolicy, optional, default: "false"
    - searchScope, optional, default: "1"
    - ldapFilter, optional, default: ""
    - authenticationType, optional, default: "simple"
    - userObjectClasses, optional, default: "person, organizationalPerson, user"
    - uuidLdapAttribute, optional, default: "cn"
    - rdnLdapAttribute, optional, default: "cn"
    - usernameLdapAttribute, optional, default: "cn"
    - importUsers, optional, default: "true"
    - editMode, optional, default: "READ_ONLY"
    - batchSize, optional, default: "1000"
    - cachePolicy, optional, default: "DEFAULT"
    - periodicChangedUsersSync, optional, default: "false"
    - priority, optional, default: "0"

#### Example
    id: add-ad-ldap
    author: klg71
    changes:
      - addAdLdap:
            realm: master
            name: testLdap
            config: 
                connectionUrl: https://testldap
                usersDN: usersTestDn
                bindCredential: testPassword
                bindDN: testBindDn
                
### DeleteUserFederation
Deletes an userFederation from the realm, throws an exception if it doesn't exist.

#### Parameters
- realm: String, optional
- name: String, not optional
                
#### Example
      - deleteUserFederation:
          realm: integ-test
          name: testLdap

# Technical Hints

The migration hashes are stored in the attribute named 'migration' in the migration user.

There are no transactions in keycloak though if the rollback fails there might be a non deterministic state!
If it fails I would like to receive a bug report for this.

If you struggle with invalid hashes on a linux-windows setup check the line endings of the json-import files as git might check them out as LF where on windows its CRLF and thus producing the error in hashing.

If you are using git you can place the following file into the dir with the json-import-files to reassure that the line ending is always LF.

`.gitattributes`:

    ** text eol=lf

# Hacking
To start developing on this project you can use the gradle tasks.

To start the local development keycloak you can just use the task ```startLocalKeycloak```

Tested with OpenJdk 12 and Keycloak 8.0.2

## Use keycloakmigration through maven dependency:
### Gradle dependency:
     // https://mvnrepository.com/artifact/de.klg71.keycloakmigration/keycloakmigration
     compile group: 'de.klg71.keycloakmigration', name: 'keycloakmigration', version: '0.1.0'
 ## Usage
 Kotlin
 
    class MyMigrationArgs(private val adminUser: String,
                          private val adminPassword: String,
                          private val migrationFile: String,
                          private val baseUrl: String,
                          private val realm: String,
                          private val clientId: String,
                          private val correctHashes: Boolean) : MigrationArgs {
        override fun adminUser() = adminUser
        override fun adminPassword() = adminPassword
        override fun baseUrl() = baseUrl
        override fun migrationFile() = migrationFile
        override fun realm() = realm
        override fun clientId() = clientId
        override fun correctHashes() = correctHashes
    }

    class KeycloakMigrationExecution  {
        fun migrate() {
            MyMigrationArgs("admin", "adminPass", "keycloak-changelog.yml",
                            "https://myauthserver", "master",
                            "admin-cli", false)
                    .let {
                        de.klg71.keycloakmigration.migrate(it)
                    }
        }

    }

# TODOS:
- Add more commands
- Add sophisticated unit and integration Tests
- batch updates for users
