package de.klg71.keycloakmigration.model

import java.util.UUID

data class Mapper(val id: UUID,
                  val name: String,
                  val protocol: String,
                  val protocolMapper: String,
                  val consentRequired: Boolean,
                  val config: Map<String, String>
)
