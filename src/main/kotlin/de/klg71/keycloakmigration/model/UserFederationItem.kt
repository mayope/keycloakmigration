package de.klg71.keycloakmigration.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserFederationItem(val id: UUID,
                              val name: String,
                              val providerId: String,
                              val providerType: String,
                              val parentId: String,
                              val config: Map<String, List<String>>)