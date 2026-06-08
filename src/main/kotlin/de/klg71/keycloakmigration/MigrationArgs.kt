package de.klg71.keycloakmigration

@Suppress("TooManyFunctions")
interface MigrationArgs {
    fun adminUser(): String
    fun adminPassword(): String
    fun adminTotp(): String
    fun adminUseOauth(): Boolean
    fun adminUseOauthLocalPort(): Int
    fun baseUrl(): String
    fun migrationFile(): String
    fun realm(): String
    fun clientId(): String
    fun clientSecret(): String?
    fun loginWithClientSecret(): Boolean
    fun correctHashes(): Boolean
    fun parameters(): Map<String, String>
    fun waitForKeycloak(): Boolean
    fun waitForKeycloakTimeout(): Long
    fun failOnUndefinedVariables(): Boolean
    fun warnOnUndefinedVariables(): Boolean
    fun disableSetUnmanagedAttributesToAdminEdit(): Boolean
}
