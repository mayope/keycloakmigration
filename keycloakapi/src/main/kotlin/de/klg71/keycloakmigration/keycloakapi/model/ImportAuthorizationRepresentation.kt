package de.klg71.keycloakmigration.keycloakapi.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize

class ImportAuthorizationRepresentationSerializer : JsonSerializer<ImportAuthorizationRepresentation>() {
    override fun serialize(value: ImportAuthorizationRepresentation?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeRaw(value?.rawJson)
    }
}

@JsonSerialize(using = ImportAuthorizationRepresentationSerializer::class)
class ImportAuthorizationRepresentation(val rawJson: String)
