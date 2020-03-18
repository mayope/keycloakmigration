package de.klg71.keycloakmigration.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserListItem(val id: UUID, val createdTimestamp: Long, val username: String, val enabled: Boolean,
                        val emailVerified: Boolean)
