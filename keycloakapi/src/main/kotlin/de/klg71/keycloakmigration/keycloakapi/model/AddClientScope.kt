package de.klg71.keycloakmigration.keycloakapi.model

data class AddClientScope(
        val name: String,
        val description: String?,
        val protocol: String,
        val attributes: Map<String, String>)

fun addClientScope(name: String, description: String?, protocol: String = "openid-connect",
                   consentScreenText: String? = null, displayOnConsentScreen: Boolean = false,
                   guiOrder: Int? = null, includeInTokenScope: Boolean = true): AddClientScope {
    val attributes = mutableMapOf<String, String>()
    consentScreenText?.let {
        attributes["consent.screen.text"] = it
    }
    attributes["display.on.screen.consent"] = displayOnConsentScreen.toString()
    guiOrder?.let {
        attributes["gui.order"] = it.toString()
    }
    attributes["include.in.token.scope"] = includeInTokenScope.toString()
    return AddClientScope(name, description, protocol, attributes)
}
