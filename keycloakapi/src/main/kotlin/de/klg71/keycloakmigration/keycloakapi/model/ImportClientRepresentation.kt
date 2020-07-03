package de.klg71.keycloakmigration.keycloakapi.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize

class ImportClientRepresentationSerializer : JsonSerializer<ImportClientRepresentation>() {
    override fun serialize(value: ImportClientRepresentation?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeRaw(value?.rawJson)
    }
}

@JsonSerialize(using = ImportClientRepresentationSerializer::class)
class ImportClientRepresentation(val rawJson: String)
