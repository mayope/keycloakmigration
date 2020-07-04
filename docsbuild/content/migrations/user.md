---
author: klg71
layout: post
title:  "User Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/user/
---

# User Migrations
All migrations referring to the user resource.
## addUser
Adds a user to keycloak. Fails if a user with that name already exists.
### Parameters
- realm: String, optional
- name: String, not optional
- enabled: Boolean,  default=true
- emailVerified: Boolean,  default=true
- attributes: Map with Layout Map<String, List< String >>, default=empty
- groups: List of groupnames to attach, List< String >, optional, default=empty
- realmRoles: List of realmroles to attach, List< String >, optional, default=empty
- clientRoles: List of ClientRoles to attach, List< ClientRole >, optional, default=empty
- email: String, optional, default=""
- firstName: String, optional, default=""
- lastName: String, optional, default=""

ClientRole Parameters:
- client: ClientId, String, not optional
- role: Rolename, String, not optional

### Example
    id: test
    author: klg71
    changes:
    - addUser:
        realm: master
        name: test
        enabled: true
        email: test@example.de
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

## deleteUser

Removes a user from keycloak. Fails if a user with that name does not exists.
### Parameters

- realm: String, optional
- name: String, not optional

### Example

    id: test
    author: klg71
    changes:
    - deleteUser:
        realm: master
        name: test
        
## updateUser

Updates an exiting user in keycloak. Fails if no user with given name exists.
### Parameters

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
    - config1: Map<String, String>, optional, default = emptyMap() (See keycloak documentation)
    
### Example

    id: test
    author: klg71
    changes:
    - updateUser:
        realm: master
        name: test
        enabled: false
        lastName: Lukas
        
### Example to update Password

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
              
#### Script to generate salt and hash:

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

## updateUserPassword

Updates the passwords of a user
> WARNING: This action can not be rolled back!

The password is hashed with 27500 hash_iterations and a key_byte_length of 64 bytes.

### Parameters
- realm: String, optional
- name: String, not optional
- password: String, not optional
- salt: String, optional, default = Random 15 letter alphanumeric String

### Example
    id: test
    author: klg71
    realm: integ-test
    changes:
      - addUser:
          name: testPasswordUser
      - updateUserPassword:
          name: testPasswordUser
          password: "testPassword"

## addUserAttribute
Adds an attribute to an existing user. Throws an error if the user does not exist.

User attributes can't be set deterministic with the updateUser action.
### Parameters
- realm: String, optional
- name: String, not optional
- attributeName: String, not optional
- attributeValues: List< String>, not optional
- override: Boolean, default=false

### Example
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

## deleteUserAttribute
Deletes an attribute to an existing user. Throws an error if the user does not exist.
### Parameters
- realm: String, optional
- name: String, not optional
- attributeName: String, not optional
- failOnMissing: Boolean, default=true

### Example
    id: test
    author: klg71
    changes:
    - deleteUserAttribute:
        realm: master
        name: test
        attributeName: test1

## assignRole
Assigns a role to the given user. Fails if the user or the role doesn't exist.
### Parameters
- realm: String, optional
- user: String, not optional
- role: String, not optional

### Example
    id: test
    author: klg71
    changes:
    - assignRole:
        realm: master
        user: testUser
        role: testRole

## revokeRole
Revokes a role from the given user. Fails if the user or the role doesn't exist or the user does not have the role assigned.

### Parameters
- realm: String, optional
- user: String, not optional
- role: String, not optional

### Example
    id: test
    author: klg71
    changes:
    - revokeRole:
        realm: master
        user: testUser
        role: testRole
        
## assignGroup
Assigns a group to the given user. Fails if the user or the group doesn't exist.
### Parameters
- realm: String, optional
- user: String, not optional
- group: String, not optional

### Example
    id: test
    author: klg71
    changes:
    - assignGroup:
        realm: master
        user: testUser
        group: testGroup
        
## revokeGroup
Revokes a group from the given user. Fails if the user or the group doesn't exist or the user doesnt have the group assigned .
### Parameters
- realm: String, optional
- user: String, not optional
- group: String, not optional

### Example
    id: test
    author: klg71
    changes:
    - revokeGroup:
        realm: master
        user: testUser
        group: testGroup
