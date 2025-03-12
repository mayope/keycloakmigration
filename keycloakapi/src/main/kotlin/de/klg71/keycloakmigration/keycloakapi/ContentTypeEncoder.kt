package de.klg71.keycloakmigration.keycloakapi

import com.fasterxml.jackson.databind.ObjectMapper
import feign.RequestTemplate
import feign.codec.EncodeException
import feign.codec.Encoder
import feign.jackson.JacksonEncoder
import java.lang.reflect.Type

class ContentTypeEncoder(
    private val objectMapper: ObjectMapper
) : Encoder {
    override fun encode(obj: Any, bodyType: Type, template: RequestTemplate) {
        val contentType = template.headers()["Content-Type"]?.firstOrNull()?.split(';')?.firstOrNull()

        if (contentType == "text/plain") {
            PlainTextEncoder().encode(obj, bodyType, template)
        } else {
            JacksonEncoder(objectMapper).encode(obj, bodyType, template)
        }
    }
}

class PlainTextEncoder : Encoder {
    override fun encode(obj: Any, bodyType: Type, template: RequestTemplate) {
        if (obj is String) {
            template.body(obj)
        } else {
            throw EncodeException("PlainTextEncoder only supports String body types.")
        }
    }
}