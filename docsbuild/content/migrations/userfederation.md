---
author: klg71
layout: post
title:  "User Federation Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/userfederation/
---
# UserFederation Migrations
All migrations referring to the userfederation resource.
## AddAdLdap
Adds an active directory to the realm

### Parameters
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

### Example
```yaml
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
```
                
## AddAdLdapFullNameMapper
Adds a full name mapper to an active directory ldap, throws an error if the ad doesn't exists
or if a mapper with this name already exists in this ad

### Parameters
- realm: String, optional
- name: String, not optional
- adName: String, not optional
- ldapFullNameAttribute: String, not optional
- readOnly: Boolean, optional, default = true 
- writeOnly: Boolean, optional, default = false
                
### Example
    id: add-ad-ldap-full-name-mapper
    author: klg71
    realm: integ-test
    changes:
      - addAdLdapFullNameMapper:
          name: testFullNamemapper
          adName: testLdap
          ldapFullNameAttribute: fullName
          
## AddAdLdapGroupMapper
Adds a group mapper to an active directory ldap, throws an error if the ad doesn't exists
or if a mapper with this name already exists in this ad

### Parameters
- realm: String, optional
- name: String, not optional
- adName: String, not optional
- groupsDn: String, not optional
- groupObjectClasses: List< String >, optional, default = emptyList()
- groupNameLdapAttribute: String, optional, default = "cn"
- preserveGroupInheritance: Boolean, optional, default = true,
- membershipLdapAttribute: String, optional, default = "member",
- membershipAttributeType: String, optional, default = "DN",
- membershipUserLdapAttribute: String, optional, default = "cn",
- filter: String, optional, default = "",
- mode: String, optional, default = "READ_ONLY",
- ignoreMissingGroups: Boolean, optional, default = false,
- userRolesRetrieveStrategy: String, optional, default = "LOAD_GROUPS_BY_MEMBER_ATTRIBUTE",
- mappedGroupAttributes: List< String >, optional, default = emptyList(),
- memberofLdapAttribute: String, optional, default = "memberOf",
- dropNonExistingGroupsDuringSync: Boolean, optional, default = false
                
### Example
    id: add-ad-ldap-group-mapper
    author: klg71
    realm: integ-test
    changes:
      - addAdLdapGroupMapper:
          name: testGroupMapper
          adName: testLdap
          groupsDn: groupsDn
          
## AddAdLdapHardcodedRoleMapper
Adds a hardcoded role mapper to an active directory ldap, throws an error if the ad doesn't exists
or if a mapper with this name already exists in this ad. If the given role doesn't exists this command throws an exception.

### Parameters
- realm: String, optional
- name: String, not optional
- adName: String, not optional
- role: String, not optional
                
### Example
    id: add-ad-ldap-hardcoded-role-mapper
    author: klg71
    realm: integ-test
    changes:
      - addRole:
          name: testMapperRole
      - addAdLdapHardcodedRoleMapper:
          name: testHardcodedRoleMapper
          adName: testLdap
          role: testMapperRole
          
## AddAdLdapUserControlMapperMapper
Adds a user account control mapper to an active directory ldap, throws an error if the ad doesn't exists
or if a mapper with this name already exists in this ad. 

### Parameters
- realm: String, optional
- name: String, not optional
- adName: String, not optional
                
### Example
    id: add-ad-ldap-user-account-control-mapper
    author: klg71
    realm: integ-test
    changes:
      - addAdLdapUserAccountControlMapper:
          name: testUserAccountControl
          adName: testLdap
          
## AddAdLdapUserAttributeMapperMapper
Adds a user account attribute mapper to an active directory ldap, throws an error if the ad doesn't exists
or if a mapper with this name already exists in this ad. 

### Parameters
- realm: String, optional
- name: String, not optional
- adName: String, not optional
- userModelAttribute: String, not optional
- ldapAttribute: String, not optional
- readOnly: Boolean, optional, default = false,
- alwaysReadFromLdap: Boolean, optional, default = false,
- isMandatoryInLdap: Boolean, optional, default = false
                
### Example
    id: add-ad-ldap-user-attribute-mapper
    author: klg71
    realm: integ-test
    changes:
      - addAdLdapUserAttributeMapper:
          name: testUserAttributeMapper
          adName: testLdap
          userModelAttribute: userModelAttribute
          ldapAttribute: ldapAttribute
          
## AddAdLdapMapperMapper
Adds a custom mapper to an active directory ldap, throws an error if the ad doesn't exists
or if a mapper with this name already exists in this ad. 

> Only use this action if you can't find a convenient method to add the mapper above.
> You can find the correct parameters by using the network debugging tool in chrome/firefox to inspect the requests in the keycloak gui.


### Parameters
- realm: String, optional
- name: String, not optional
- adName: String, not optional
- providerId: String, not optional
- config: Map<String,String>, not optional
                
### Example
    id: add-ad-ldap-user-attribute-mapper
    author: klg71
    realm: integ-test
    changes:
      - addAdLdapMapper:
          name: testRoleMapper
          adName: testLdap
          providerId: role-ldap-mapper
          config:
              memberof.ldap.attribute: "memberOf"
              membership.attribute.type: "DN"
              membership.ldap.attribute: "member"
              membership.user.ldap.attribute: "cn"
              mode: "READ_ONLY"
              role.name.ldap.attribute: "cn"
              role.object.classes: "group"
              roles.dn: "rolesDn"
              use.realm.roles.mapping: "true"
              user.roles.retrieve.strategy: "LOAD_ROLES_BY_MEMBERSHIP_ATTRIBUTE"
          
                
## DeleteUserFederation
Deletes an userFederation from the realm, throws an exception if it doesn't exist.

### Parameters
- realm: String, optional
- name: String, not optional
                
### Example
    id: delete-ad-ldap
    author: klg71
    changes:
      - deleteUserFederation:
          realm: integ-test
          name: testLdap
