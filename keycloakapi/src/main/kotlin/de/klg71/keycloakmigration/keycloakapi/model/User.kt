package de.klg71.keycloakmigration.keycloakapi.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

data class UserAccess(val impersonate: Boolean,
    val manage: Boolean,
    val manageGroupMembership: Boolean,
    val mapRoles: Boolean,
    val view: Boolean)

data class ResetPassword(
    val value: String,
    val temporary: Boolean = false,
    val type: String = "Password",
)

data class User(
    val id: UUID,
    val createdTimestamp: Long,
    val username: String,
    val enabled: Boolean,
    val emailVerified: Boolean,
    val attributes: Attributes? = null,
    val notBefore: Long,
    val totp: Boolean,
    val access: UserAccess? = null,
    val disableableCredentialTypes: List<String>,
    val requiredActions: List<String>,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val credentials: List<UserCredential>? = null,
    val federationLink: String? = null
)

class UpdateUserBuilder(existingUser: User) {
    var id: UUID = existingUser.id
    var createdTimestamp: Long = existingUser.createdTimestamp
    var username: String = existingUser.username
    var enabled: Boolean = existingUser.enabled
    var emailVerified: Boolean = existingUser.emailVerified
    var attributes: Attributes? = existingUser.attributes
    var notBefore: Long = existingUser.notBefore
    var totp: Boolean = existingUser.totp
    var access: UserAccess? = existingUser.access
    var disableableCredentialTypes: List<String> = existingUser.disableableCredentialTypes
    var requiredActions: List<String> = existingUser.requiredActions
    var email: String? = existingUser.email
    var firstName: String? = existingUser.firstName
    var lastName: String? = existingUser.lastName
    var credentials: List<UserCredential>? = existingUser.credentials
    var federationLink: String? = existingUser.federationLink

    fun build() = User(
        id,
        createdTimestamp,
        username,
        enabled,
        emailVerified,
        attributes,
        notBefore,
        totp,
        access,
        disableableCredentialTypes,
        requiredActions,
        email,
        firstName,
        lastName,
        credentials,
        federationLink
    )
}

typealias Attributes = Map<String, List<String>>
