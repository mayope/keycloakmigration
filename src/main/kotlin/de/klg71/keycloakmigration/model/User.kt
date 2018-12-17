package de.klg71.keycloakmigration.model

import java.util.*

data class UserAccess(val impersonate: Boolean,
                      val manage: Boolean,
                      val manageGroupMembership: Boolean,
                      val mapRoles: Boolean,
                      val view: Boolean)

data class User(val id: UUID,
                val createdTimestamp: Long,
                val username: String,
                val enabled: Boolean,
                val emailVerified: Boolean,
                val attributes: Attributes?,
                val notBefore: Long,
                val totp: Boolean,
                val access: UserAccess,
                val disableableCredentialTypes: List<String>,
                val requiredActions: List<String>,
                val email: String?,
                val firstName: String?,
                val lastName: String?)

typealias Attributes = Map<String, List<String>>