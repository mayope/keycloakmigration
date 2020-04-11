# Keycloakmigration ![Maven metadata URL](https://img.shields.io/maven-metadata/v/https/repo.maven.apache.org/maven2/de/klg71/keycloakmigration/keycloakmigration/maven-metadata.xml.svg?label=mavenCentral)

This projects aims to automate the configuration of the keycloak authentication provider.
It is inspired by the database migration tool liquibase.
The goal is to provide a similar mechanism for Keycloak. There also exists a gradle plugin for automated build pipelines: [keycloakmigrationplugin](https://github.com/klg71/keycloakmigrationplugin).

- [Usage](#usage)
- [Migration Files](#migration-files)
  * [Changelog](#changelog)
    + [Parameters](#parameters)
    + [Example](#example)
  * [ChangeSet](#changeset)
    + [Parameters](#parameters-1)
    + [Example](#example-1)
- [Parameter substitution](#parameter-substitution)
    + [Example](#example-2)
- [Troubleshooting hashes](#troubleshooting-hashes)
- [Supported migrations](#supported-migrations)
  * [User Migrations](#user-migrations)
    + [addUser](#adduser)
      - [Parameters](#parameters-2)
      - [Example](#example-3)
    + [deleteUser](#deleteuser)
      - [Parameters](#parameters-3)
      - [Example](#example-4)
    + [updateUser](#updateuser)
      - [Parameters](#parameters-4)
      - [Example](#example-5)
      - [Example to update Password](#example-to-update-password)
        * [Script to generate salt and hash:](#script-to-generate-salt-and-hash-)
    + [updateUserPassword](#updateuserpassword)
      - [Parameters](#parameters-5)
      - [Example](#example-6)
    + [addUserAttribute](#adduserattribute)
      - [Parameters](#parameters-6)
      - [Example](#example-7)
    + [deleteUserAttribute](#deleteuserattribute)
      - [Parameters](#parameters-7)
      - [Example](#example-8)
    + [assignRole](#assignrole)
      - [Parameters](#parameters-8)
      - [Example](#example-9)
    + [revokeRole](#revokerole)
      - [Parameters](#parameters-9)
      - [Example](#example-10)
    + [assignGroup](#assigngroup)
      - [Parameters](#parameters-10)
      - [Example](#example-11)
    + [revokeGroup](#revokegroup)
      - [Parameters](#parameters-11)
      - [Example](#example-12)
  * [Group Migrations](#group-migrations)
    + [addGroup](#addgroup)
      - [Parameters](#parameters-12)
      - [Example](#example-13)
    + [deleteGroup](#deletegroup)
      - [Parameters](#parameters-13)
      - [Example](#example-14)
    + [updateGroup](#updategroup)
      - [Parameters](#parameters-14)
      - [Example](#example-15)
    + [assignRoleToGroup](#assignroletogroup)
      - [Parameters](#parameters-15)
      - [Example](#example-16)
    + [revokeRoleFromGroup](#revokerolefromgroup)
      - [Parameters](#parameters-16)
      - [Example](#example-17)
  * [Role Migrations](#role-migrations)
    + [addRole](#addrole)
      - [Parameter](#parameter)
      - [Example](#example-18)
    + [deleteRole](#deleterole)
      - [Parameter](#parameter-1)
      - [Example](#example-19)
  * [Client Migrations](#client-migrations)
    + [addSimpleClient](#addsimpleclient)
      - [Parameter](#parameter-2)
      - [Example](#example-20)
    + [deleteClient](#deleteclient)
      - [Parameter](#parameter-3)
      - [Example](#example-21)
    + [importClient](#importclient)
      - [Parameters](#parameters-17)
      - [Example](#example-22)
    + [updateClient](#updateclient)
      - [Parameters](#parameters-18)
      - [Example](#example-23)
  * [Realm Migrations](#realm-migrations)
    + [addRealm](#addrealm)
      - [Parameters](#parameters-19)
      - [Example](#example-24)
    + [deleteRealm](#deleterealm)
      - [Parameters](#parameters-20)
      - [Example](#example-25)
    + [updateRealm](#updaterealm)
      - [Parameters](#parameters-21)
      - [Example](#example-26)
  * [User Federation Migrations](#user-federation-migrations)
    + [AddAdLdap](#addadldap)
      - [Parameters](#parameters-22)
      - [Example](#example-27)
    + [DeleteUserFederation](#deleteuserfederation)
      - [Parameters](#parameters-23)
      - [Example](#example-28)
- [Technical Hints](#technical-hints)
- [Hacking](#hacking)
  * [Use keycloakmigration through maven dependency](#use-keycloakmigration-through-maven-dependency)
    + [Gradle dependency](#gradle-dependency)
  * [Usage](#usage-1)
- [TODOS](#todos)
## Usage
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

## Migration Files
There are two types of files to control migrations in keycloak. ChangeLog and ChangeSet (may sound similar in liquibase).
The Changelog references all changeSets to apply and the ChangeSets contain the actual changes.



### Changelog
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
The changeSet contains the actual changes as a list of migrations (see [Supported Migrations](#supported-migrations))

#### Parameters
- id: String, not optional, describe the change
- author: String, not optional, author of the changeset
- realm: String, optional, preset realm for actions
> Please note that you must either provide a realm in the ChangeSet or in each action! Even though both parameters are optional one must be set!

- changes: List of Migrations

#### Example

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
        
## Parameter substitution
This format supports substitution of environment variables for dynamic content. The hash however will not include the resolved variable and just encode the file as a hash.
#### Example

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


## Troubleshooting hashes
The hash implementation from 0.0.12 to 0.1.0 has changed so the old hashes will always throw an error.
An hash error may also occur if you did make a syntactic but not semantic change to the changelog.
You can however call the migration script with the `--correct-hashes` switch and it will just replace the failing hashes.
This will skip any control mechanism and you must to make sure that you have the same changelog that you migrated before.
It will only check for the number of hashes to skip or execute migrations!

> **Dont use the `--correct-hashes` switch in build pipelines!**

## Supported migrations
This are the currently implemented commands. I hope I can find the time to implement more of them.

For more examples see `src/test/resources/changesets`.
### User Migrations
#### addUser
Adds a user to keycloak. Fails if a user with that name already exists.
##### Parameters
- realm: String, optional
- name: String, not optional
- enabled: Boolean,  default=true
- emailVerified: Boolean,  default=true
- attributes: Map with Layout Map<String, List< String >>, default=empty
- groups: List of groupnames to attach, List< String >, optional, default=empty
- realmRoles: List of realmroles to attach, List< String >, optional, default=empty
- clientRoles: List of ClientRoles to attach, List< ClientRole >, optional, default=empty

ClientRole Parameters:
- client: ClientId, String, not optional
- role: Rolename, String, not optional
##### Example
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
          groups:
            - testGroup
          realmRoles:
            - testRealmRole
          clientRoles:
            - client: testClient
              role: testClientRole

#### deleteUser
Removes a user from keycloak. Fails if a user with that name does not exists.
##### Parameters
- realm: String, optional
- name: String, not optional
##### Example
    id: test
    author: klg71
    changes:
    - deleteUser:
        realm: master
        name: test
#### updateUser
Updates an exiting user in keycloak. Fails if no user with given name exists.
##### Parameters
- realm: String, optional
- name: String, not optional
- enabled: Boolean, default=no change
- emailVerified: Boolean, default=no change
- access: String, default=no change
- notBefore: Long, default=no change
- totp: Boolean, default=no change
- disableableCredentialTypes: List< String >, default=no change
- requiredActions: List< String >, default=no change
- email: String, default=no change
- firstName: String, default=no change
- lastName: String, default=no change
- credentials: Map<String,String> (see example below)
    - hashedSaltedValue: String, not optional
    - salt: String, not optional
    - algorithm: String, optional, default = "pbkdf2-sha256"
    - counter: Int, optional, default = 0,
    - createdDate: Long, optional, default = Date().time,
    - digits: Int, optional, default = 0,
    - hashIterations: Int, optional, default = 27500,
    - period: Int, optional, default = 0,
    - type: String, optional, default = "password",
    - config: Map<String, String>, optional, default = emptyMap() (See keycloak documentation)
##### Example
    id: test
    author: klg71
    changes:
    - updateUser:
        realm: master
        name: test
        enabled: false
        lastName: Lukas
        
##### Example to update Password
> If you don't want to hash and generate the salt by youself you can use the [updateUserPassword](#updateuserpassword) method listed below.
>
> This method gives more control over the credential entry in keycloak including hashIterations, algorithms used, digits and additional configs.
>
> Updating the credential can not be rolled back!

    id: update-password
    author: klg71
    changes:
      - updateUser:
          realm: integ-test
          name: test
          credentials:
            - hashedSaltedValue: 1tWf95Drz6t8/9kKE3tiJXPywCzG/C0KDnmCIFXEDdFQMPB6iVWWxjLO9HJI3YwTfWZa78N+hcmYHcT1tkavcA==
              salt: dGVzdB==
              
###### Script to generate salt and hash:
```kotlin
import org.apache.commons.codec.Charsets.UTF_8
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

fun generatePassword(){
        println("Keycloak password hash helper")
        val scanner = Scanner(System.`in`, UTF_8)
        println("Enter password:")
        val password = scanner.nextLine()
        println("Enter salt:")
        val salt = scanner.nextLine()

        val hashIterations = 27500
        val keyByteLength = 64
        val pass = getEncryptedPassword(password, salt.toByteArray(UTF_8), hashIterations, keyByteLength)
        println("Password: $pass")
        println("Salt: " + Base64.getEncoder().encodeToString(salt.toByteArray(UTF_8)))
}
fun getEncryptedPassword(password: String, salt: ByteArray,
                         iterations: Int, derivedKeyLength: Int): String {
    return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").run {
        generateSecret(PBEKeySpec(password.toCharArray(), salt,
                iterations, derivedKeyLength * 8)
        )
    }.run {
        @Suppress("UsePropertyAccessSyntax")
        getEncoded()
    }.let {
        Base64.getEncoder().encodeToString(it)
    }

}
```

#### updateUserPassword
Updates the passwords of a user
> WARNING: This action can not be rolled back!

The password is hashed with 27500 hash_iterations and a key_byte_length of 64 bytes.

##### Parameters
- realm: String, optional
- name: String, not optional
- password: String, not optional
- salt: String, optional, default = Random 15 letter alphanumeric String

##### Example
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

#### addUserAttribute
Adds an attribute to an existing user. Throws an error if the user does not exist.

User attributes can't be set deterministic with the updateUser action.
##### Parameters
- realm: String, optional
- name: String, not optional
- attributeName: String, not optional
- attributeValues: List< String>, not optional
- override: Boolean, default=false

##### Example
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

#### deleteUserAttribute
Deletes an attribute to an existing user. Throws an error if the user does not exist.
##### Parameters
- realm: String, optional
- name: String, not optional
- attributeName: String, not optional
- failOnMissing: Boolean, default=true

##### Example
    id: test
    author: klg71
    changes:
    - deleteUserAttribute:
        realm: master
        name: test
        attributeName: test1

#### assignRole
Assigns a role to the given user. Fails if the user or the role doesn't exist.
##### Parameters
- realm: String, optional
- user: String, not optional
- role: String, not optional

##### Example
    id: test
    author: klg71
    changes:
    - assignRole:
        realm: master
        user: testUser
        role: testRole

#### revokeRole
Revokes a role from the given user. Fails if the user or the role doesn't exist or the user does not have the role assigned.

##### Parameters
- realm: String, optional
- user: String, not optional
- role: String, not optional

##### Example
    id: test
    author: klg71
    changes:
    - revokeRole:
        realm: master
        user: testUser
        role: testRole
        
#### assignGroup
Assigns a group to the given user. Fails if the user or the group doesn't exist.
##### Parameters
- realm: String, optional
- user: String, not optional
- group: String, not optional

##### Example
    id: test
    author: klg71
    changes:
    - assignGroup:
        realm: master
        user: testUser
        group: testGroup
        
#### revokeGroup
Revokes a group from the given user. Fails if the user or the group doesn't exist or the user doesnt have the group assigned .
##### Parameters
- realm: String, optional
- user: String, not optional
- group: String, not optional

##### Example
    id: test
    author: klg71
    changes:
    - revokeGroup:
        realm: master
        user: testUser
        group: testGroup

### Group Migrations
#### addGroup
Adds a new group to keycloak. Fails if the group already exists.

##### Parameters
- realm: String, optional
- name: String, not optional
- parent: String, default=empty

##### Example
    id: test
    author: klg71
    changes:
    - addGroup:
        realm: master
        name: testGroup

#### deleteGroup
Removes a group from keycloak. Fails if the group does not exist.

##### Parameters
- realm: String, optional
- name: String, not optional

##### Example
    id: test
    author: klg71
    changes:
    - deleteGroup:
        realm: master
        name: testUser

#### updateGroup
Updates a group from keycloak. Fails if the group does not exist.

##### Parameters
- realm: String, optional
- name: String, not optional
- attributes: Map< String,List< String>>, optional, default=existing attributes
- realmRoles: List< String>, optional, default=existing realm roles
- clientRoles: Map< String,List< String>>, optional, default=existing client roles, Key of the map is the clientId and the value is a List of roleNames to attach

##### Example
    id: test
    author: klg71
    changes:
      - updateGroup:
          realm: master
          name: child1
          attributes:
            lkz:
              - "1234"
              
#### assignRoleToGroup
Assigns a role to a group in keycloak. Fails if the group or the role does not exist.

##### Parameters
- realm: String, optional
- role: String, not optional
- group: String, not optional
- clientId: String, optional, default=realmRole

##### Example
    id: test
    author: klg71
    changes:
      - assignRoleToGroup:
          realm: integ-test
          role: parent
          group: test3
          
#### revokeRoleFromGroup
Revokes a role from a group in keycloak. Fails if the group or the role does not exist or the role is not assigned to the group.

##### Parameters
- realm: String, optional
- role: String, not optional
- group: String, not optional
- clientId: String, optional, default=realmRole

##### Example
    id: test
    author: klg71
    changes:
      - revokeRoleFromGroup:
          realm: integ-test
          group: parent
          role: test3

### Role Migrations
#### addRole
Add a role to keycloak, fails if the role already exists
##### Parameter
- realm: String, optional
- name: String, not optional,
- clientId: String, optional, default=realmRole,
- description: String, optional, default=""
- attributes: Map< String,List< String>>, optional, default=empty
- composite: Boolean, optional, default=false
- clientRole: Boolean, optional, default=false,
- containerId: String, optional, default=not set

##### Example
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
#### deleteRole
Delete a role from keycloak, fails if the role does not exist
##### Parameter
- realm: String, optional
- name: String, not optional,
- clientId: String, optional, default=realmRole
##### Example
    id: delete-role
    author: klg71
    changes:
    - deleteRole:
        realm: master
        name: test4
        clientId: test

### Client Migrations
#### addSimpleClient
Simple command to add a client to keycloak, TODO: add more fields
##### Parameter
- realm: String, optional
- clientId: String, not optional,
- enabled: Boolean, optional, default=true
- attributes: Map< String, String>, optional, default = empty
- protocol: String, optional, default="openid-connect"
- secret: String, optional
- publicClient: Boolean, optional, default=true
- redirectUris: List< String>, optional, default=empty
##### Example
    id: add-simple-client
    author: klg71
    changes:
    - addSimpleClient:
        realm: master
        clientId: test

#### deleteClient
Delete a client in keycloak
##### Parameter
- realm: String, optional
- clientId: String, not optional,
##### Example
    id: delete-client
    author: klg71
    changes:
    - deleteClient:
        realm: master
        clientId: test

#### importClient
Imports a client using the json representation.

##### Parameters
- realm: String, optional
- clientRepresentationJsonFilename: String, not optional
- relativeToFile: Boolean, optional, default=true

##### Example
    id: import-client
    author: klg71
    changes:
    - importClient:
          realm: master
          clientRepresentationJsonFilename: client.json
          relativeToFile: true

#### updateClient
Update a client

##### Parameters
- realm: String, optional
- clientId: String, not optional
- name: String, optional, default=no change
- description: String, optional, default=no change
- enabled: Boolean, optional, default=no change
- attributes: Map<String, String>, optional, default=no change
- protocol: String, optional, default=no change
- redirectUris: List< String>, optional, default=no change
- bearerOnly: Boolean, optional, default=no change
- directAccessGrantEnabled: Boolean, optional, default=no change
- implicitFlowEnabled: Boolean, optional, default=no change
- standardFlowEnabled: Boolean, optional, default=no change
- adminUrl: String, optional, default=no change
- baseUrl: String, optional, default=no change
- rootUrl: String, optional, default=no change
- publicClient: Boolean, optional, default=no change
- serviceAccountsEnabled: Boolean, optional, default=no change

##### Example
    id: update-client
    author: klg71
    changes:
    - updateClient:
        realm: master
        clientId: testClient
        redirectUris: 
            - http://localhost:8080
            - https://www.example.com
            
            
### Realm Migrations

#### addRealm
adds a Realm, throws error if realm with that id already exists

##### Parameters
- name: String, not optional
- enabled: Boolean, optional, default=true
- id: String, optional, default=name

##### Example
    id: add-realm
    author: klg71
    changes:
      - addRealm:
          name: integ-test
          
#### deleteRealm
deletes a Realm, throws error if realm with that id does not exists

##### Parameters
- id: String, not optional

##### Example
    id: add-realm
    author: klg71
    changes:
      - deleteRealm:
          id: integ-test
          
#### updateRealm
updates a Realm, throws error if realm with that id does not exists

##### Parameters
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
- defaultRoles:List< String>, optional
- requiredCredentials:List< String>, optional
- otpPolicyType:String, optional
- otpPolicyAlgorithm:String, optional
- otpPolicyInitialCounter:Int, optional
- otpPolicyDigits:Int, optional
- otpPolicyLookAheadWindow:Int, optional
- otpPolicyPeriod:Int, optional
- otpSupportedApplications:List< String>, optional
- webAuthnPolicyRpEntityName:String, optional
- webAuthnPolicySignatureAlgorithms:List< String>, optional
- webAuthnPolicyRpId:String, optional
- webAuthnPolicyAttestationConveyancePreference:String, optional
- webAuthnPolicyAuthenticatorAttachment:String, optional
- webAuthnPolicyRequireResidentKey:String, optional
- webAuthnPolicyUserVerificationRequirement:String, optional
- webAuthnPolicyCreateTimeout:Int, optional
- webAuthnPolicyAvoidSameAuthenticatorRegister:Boolean, optional
- webAuthnPolicyAcceptableAaguids:List< String>,
- browserSecurityHeaders:Map<String,String>, optional
- smtpServer:Map<String,String>, optional
- eventsEnabled:Boolean, optional
- eventsListeners:List< String>, optional
- enabledEventTypes:List< String>, optional
- adminEventsEnabled:Boolean, optional
- adminEventsDetailsEnabled:Boolean, optional
- internationalizationEnabled:Boolean, optional
- supportedLocales:List< String>, optional
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

##### Example
    id: update-realm
    author: klg71
    changes:
      - updateRealm:
          id: integ-test
          displayName: UpdatedRealm
          

### User Federation Migrations
#### AddAdLdap
Adds an active directory to the realm

##### Parameters
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

##### Example
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
                
#### DeleteUserFederation
Deletes an userFederation from the realm, throws an exception if it doesn't exist.

##### Parameters
- realm: String, optional
- name: String, not optional
                
##### Example
      - deleteUserFederation:
          realm: integ-test
          name: testLdap

## Technical Hints

The migration hashes are stored in the attribute named 'migration' in the migration user.

There are no transactions in keycloak though if the rollback fails there might be a non deterministic state!
If it fails I would like to receive a bug report for this.

If you struggle with invalid hashes on a linux-windows setup check the line endings of the json-import files as git might check them out as LF where on windows its CRLF and thus producing the error in hashing.

If you are using git you can place the following file into the dir with the json-import-files to reassure that the line ending is always LF.

`.gitattributes`:

    ** text eol=lf

## Hacking
To start developing on this project you can use the gradle tasks.

To start the local development keycloak you can just use the task ```startLocalKeycloak```

Tested with OpenJdk 12 and Keycloak 8.0.2

### Use keycloakmigration through maven dependency
#### Gradle dependency
     // https://mvnrepository.com/artifact/de.klg71.keycloakmigration/keycloakmigration
     compile group: 'de.klg71.keycloakmigration', name: 'keycloakmigration', version: 'x.x.x'
 ### Usage
 Kotlin
 
    class MyMigrationArgs(private val adminUser: String,
                          private val adminPassword: String,
                          private val migrationFile: String,
                          private val baseUrl: String,
                          private val realm: String,
                          private val clientId: String,
                          private val correctHashes: Boolean,
                          private val parameters: Map<String, String>) : MigrationArgs {
        override fun adminUser() = adminUser
        override fun adminPassword() = adminPassword
        override fun baseUrl() = baseUrl
        override fun migrationFile() = migrationFile
        override fun realm() = realm
        override fun clientId() = clientId
        override fun correctHashes() = correctHashes
        override fun parameters() = parameters
    }

    class KeycloakMigrationExecution  {
        fun migrate() {
            MyMigrationArgs("admin", "adminPass", "keycloak-changelog.yml",
                            "https://myauthserver", "master",
                            "admin-cli", false, emptyMap())
                    .let {
                        de.klg71.keycloakmigration.migrate(it)
                    }
        }

    }

## TODOS
- Add more commands
- Add sophisticated unit and integration Tests
- batch updates for users
