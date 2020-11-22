package de.klg71.keycloakmigration.keycloakapi

import com.fasterxml.jackson.annotation.JsonValue

enum class EmailActions(
    @JsonValue
    val jsonValue: String) {
    VERIFY_EMAIL("VERIFY_EMAIL"),
    UPDATE_PASSWORD("UPDATE_PASSWORD"),
    CONFIGURE_TOTP("CONFIGURE_TOTP"),
    UPDATE_PROFILE("UPDATE_PROFILE"),
    UPDATE_USER_LOCALE("update_user_locale")
}
