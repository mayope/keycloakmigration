package de.klg71.keycloakmigration.keycloakapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserFederation(val id: String,
                          val name: String,
                          val providerId: String,
                          val providerType: String,
                          val parentId: String,
                          val config: Map<String, List<String>>)
