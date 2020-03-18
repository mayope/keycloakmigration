package de.klg71.keycloakmigration.model

import java.util.*


data class UserCredential(val algorithm: String? = "pbkdf2-sha256",
                          val counter: Int? = 0,
                          val createdDate: Long? = Date().time,
                          val digits: Int? = 0,
                          val hashIterations: Int? = 27500,
                          val hashedSaltedValue: String?,
                          val period: Int? = 0,
                          val salt: String?,
                          val type: String? = "password",
                          val config: Map<String, String>? = emptyMap())
