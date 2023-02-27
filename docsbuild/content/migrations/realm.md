---
author: klg71
layout: post
title:  "Realm Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/realm/
---
# Realm Migrations
All migrations referring to the realm resource.
## addRealm
adds a Realm, throws error if realm with that id already exists

### Parameters
- name: String, not optional
- enabled: Boolean, optional, default=true
- id: String, optional, default=name

### Example
```yaml
    id: add-realm
    author: klg71
    changes:
      - addRealm:
          name: integ-test
```

## deleteRealm
deletes a Realm, throws error if realm with that id does not exists

### Parameters
- id: String, not optional

### Example
```yaml
    id: add-realm
    author: klg71
    changes:
      - deleteRealm:
          id: integ-test
```

## updateRealm
updates a Realm, throws error if realm with that id does not exists

### Parameters
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
- eventsExpiration:Int, optional
- enabledEventTypes:List< String>, optional
- adminEventsEnabled:Boolean, optional
- adminEventsDetailsEnabled:Boolean, optional
- internationalizationEnabled:Boolean, optional
- supportedLocales:List< String>, optional
- defaultLocale: String, optional
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
- accountTheme:String, optional
- adminTheme:String, optional
- emailTheme:String, optional
- loginTheme:String, optional

### Example
```yaml
    id: update-realm
    author: klg71
    changes:
      - updateRealm:
          id: integ-test
          displayName: UpdatedRealm
```
