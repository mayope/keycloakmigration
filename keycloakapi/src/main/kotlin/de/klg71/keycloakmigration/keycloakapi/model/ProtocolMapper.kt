package de.klg71.keycloakmigration.keycloakapi.model

import java.util.*


data class ProtocolMapper(val id: UUID?,
                          val name: String,
                          val protocol: String,
                          val protocolMapper: String,
                          val consentRequired: Boolean,
                          val config: Map<String, String>)
