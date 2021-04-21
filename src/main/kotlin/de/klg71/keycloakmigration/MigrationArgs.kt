package de.klg71.keycloakmigration

@Suppress("TooManyFunctions")
interface MigrationArgs {
    fun adminUser(): String
    fun adminPassword(): String
    fun adminTotp(): String
    fun baseUrl(): String
    fun migrationFile(): String
    fun realm(): String
    fun clientId(): String
    fun correctHashes(): Boolean
    fun parameters(): Map<String, String>
    fun waitForKeycloak(): Boolean
    fun waitForKeycloakTimeout(): Long
    fun failOnUndefinedVariables(): Boolean
    fun warnOnUndefinedVariables(): Boolean
}
