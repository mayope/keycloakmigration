---
author: klg71
layout: post
title:  "Flow Migrations"
date:   2020-07-03 12:22:20 +0200
permalink: /migrations/flow/
---
# Flow Migrations
All migrations referring to the Authentication Flow resource.
## AddFlow
Adds an authentication flow.

### Parameters
- realm: String, optional
- alias: String, not optional
- description: String, optional, default = "",
- buildIn: Boolean, optional, default = false,
- providerId: String, optional, default = "basic-flow",
- topLevel: Boolean, optional, default = true,
- executions: List< AuthenticationExecutionImport >, default = emptyList()

#### subclass AuthenticationExecutionImport
- requirement: Flow.Requirement = "ALTERNATIVE" | "DISABLED" | "REQUIRED" | "CONDITIONAL" | "OPTIONAL"
- providerId: String = see providers in next point
- level: Int
- index: Int
- config: Map<String, String>, optional, default = empty

#### Known Flow providers on Release (09.04.2021)
- no-cookie-redirect,Browser Redirect/Refresh,Perform a 302 redirect to get user agent's current URI on authenticate path with an auth_session_id query parameter.  This is for client's that do not support cookies.
- auth-cookie,Cookie,Validates the SSO cookie set by the auth server.
- reset-credentials-choose-user,Choose User,Choose a user to reset credentials for
- direct-grant-validate-password,Password,Validates the password supplied as a 'password' form parameter in direct grant request
- webauthn-authenticator,WebAuthn Authenticator,Authenticator for WebAuthn. Usually used for WebAuthn two-factor authentication
- auth-spnego,Kerberos,Initiates the SPNEGO protocol.  Most often used with Kerberos.
- reset-password,Reset Password,Sets the Update Password required action if execution is REQUIRED.  Will also set it if execution is OPTIONAL and the password is currently configured for it.
- direct-grant-auth-x509-username,X509/Validate Username,Validates username and password from X509 client certificate received as a part of mutual SSL handshake.
- auth-password-form,Password Form,Validates a password from login form.
- docker-http-basic-authenticator,Docker Authenticator,Uses HTTP Basic authentication to validate docker users, returning a docker error token on auth failure
- idp-username-password-form,Username Password Form for identity provider reauthentication,Validates a password from login form. Username may be already known from identity provider authentication
- idp-email-verification,Verify existing account by Email,Email verification of existing Keycloak user, that wants to link his user account with identity provider
- idp-auto-link,Automatically set existing user,Automatically set existing user to authentication context without any verification
- auth-x509-client-username-form,X509/Validate Username Form,Validates username and password from X509 client certificate received as a part of mutual SSL handshake.
- conditional-user-role,Condition - user role,Flow is executed only if user has the given role.
- basic-auth,Basic Auth Challenge,Challenge-response authentication using HTTP BASIC scheme.
- identity-provider-redirector,Identity Provider Redirector,Redirects to default Identity Provider or Identity Provider specified with kc_idp_hint query parameter
- direct-grant-validate-username,Username Validation,Validates the username supplied as a 'username' form parameter in direct grant request
- reset-otp,Reset OTP,Sets the Configure OTP required action.
- conditional-user-configured,Condition - user configured,Executes the current flow only if authenticators are configured
- webauthn-authenticator-passwordless,WebAuthn Passwordless Authenticator,Authenticator for Passwordless WebAuthn authentication
- basic-auth-otp,Basic Auth Password+OTP,Challenge-response authentication using HTTP BASIC scheme.  Password param should contain a combination of password + otp. Realm's OTP policy is used to determine how to parse this. This SHOULD NOT BE USED in conjection with regular basic auth provider.
- idp-review-profile,Review Profile,User reviews and updates profile data retrieved from Identity Provider in the displayed form
- idp-confirm-link,Confirm link existing account,Show the form where user confirms if he wants to link identity provider with existing account or rather edit user profile data retrieved from identity provider to avoid conflict
- auth-conditional-otp-form,Conditional OTP Form,Validates a OTP on a separate OTP form. Only shown if required based on the configured conditions.
- auth-username-password-form,Username Password Form,Validates a username and password from login form.
- reset-credential-email,Send Reset Email,Send email to user and wait for response.
- auth-username-form,Username Form,Selects a user from his username.
- http-basic-authenticator,HTTP Basic Authentication,Validates username and password from Authorization HTTP header
- auth-otp-form,OTP Form,Validates a OTP on a separate OTP form.
- direct-grant-validate-otp,OTP,Validates the one time password supplied as a 'totp' form parameter in direct grant request
- idp-create-user-if-unique,Create User If Unique,Detect if there is existing Keycloak account with same email like identity provider. If no, create new user

### Example
```yaml
id: add-flow
author: klg71
realm: integ-test
changes:
  - addFlow:
      alias: trust-foreign-idp
      executions:
        - requirement: ALTERNATIVE
          providerId: idp-create-user-if-unique
        - requirement: ALTERNATIVE
          providerId: idp-auto-link
```
## UpdateFlow
Updates an authentication flow in place.
Only updates provided values.
For an update of the flow `alias` use the oldAlias as `alias` and the newAlias in `newAlias`.

### Parameters
- realm: String, optional
- alias: String, not optional
- newAlias: String, optional, default = no update
- description: String, default = no update
- buildIn: Boolean, optional, default = no update,
- providerId: String, optional, default = no update,
- topLevel: Boolean, optional, default = no update,
- executions: List< AuthenticationExecutionImport >, default = no update

#### subclass AuthenticationExecutionImport
- requirement: Flow.Requirement = "ALTERNATIVE" | "DISABLED" | "REQUIRED" | "CONDITIONAL" | "OPTIONAL"
- providerId: String = see providers in AddFlow action
- level: Int
- index: Int
- config: Map<String, String>, optional, default = empty


### Example
```yaml
id: update-flow
author: klg71
realm: integ-test
changes:
  - addFlow:
      alias: trust-foreign-idp
      executions:
        - requirement: ALTERNATIVE
          providerId: idp-create-user-if-unique
        - requirement: ALTERNATIVE
          providerId: idp-auto-link
  - updateFlow:
      alias: trust-foreign-idp
      newAlias: trust-foreign-idp-update
      description: new-description
      executions:
        - requirement: ALTERNATIVE
          providerId: idp-create-user-if-unique
        - requirement: REQUIRED
          providerId: console-username-password
```
## DeleteFlow
Deletes a flow, if one with this alias exists

### Parameters
- realm: String, optional
- alias: String, not optional

### Example
```yaml
id: delete-flow
author: klg71
realm: integ-test
changes:
  - addFlow:
    alias: trust-foreign-idp
    executions:
      - requirement: ALTERNATIVE
        providerId: idp-create-user-if-unique
      - requirement: ALTERNATIVE
        providerId: idp-auto-link
  - deleteFlow:
    alias: trust-foreign-idp
```

## CopyFlow
Copies an authentication flow

### Parameters
- realm: String, optional
- flowAlias: String, not optional
- newName: String, not optional

### Example
```yaml
id: copy-flow
author: abigail.cortis
realm: integ-test
changes:
  - copyFlow:
      flowAlias: browser
      newName: New Authentication Flow
```
