package de.klg71.keycloakmigrationplugin

import de.klg71.keycloakmigration.MigrationArgs

open class GradleMigrationArgs(
    private val adminUser: String,
    private val adminPassword: String,
    private val adminTotp: String,
    private val adminUseOauth: Boolean,
    private val adminUseOauthLocalPort: Int,
    private val migrationFile: String,
    private val baseUrl: String,
    private val realm: String,
    private val clientId: String,
    private val clientSecret: String,
    private val loginWithClientSecret: Boolean,
    private val correctHashes: Boolean,
    private val parameters: Map<String, String>,
    private val waitForKeycloak: Boolean,
    private val waitForKeycloakTimeout: Long,
    private val failOnUndefinedVariables: Boolean,
    private val warnOnUndefinedVariables: Boolean,
    private val disableSetUnmanagedAttributesToAdminEdit: Boolean,
) : MigrationArgs {
    override fun adminUser() = adminUser
    override fun adminPassword() = adminPassword
    override fun adminTotp() = adminTotp
    override fun adminUseOauth() = adminUseOauth
    override fun adminUseOauthLocalPort() = adminUseOauthLocalPort
    override fun baseUrl() = baseUrl
    override fun migrationFile() = migrationFile
    override fun parameters() = parameters

    override fun realm() = realm
    override fun clientId() = clientId
    override fun clientSecret(): String? = clientSecret

    override fun loginWithClientSecret() = loginWithClientSecret

    override fun correctHashes() = correctHashes
    override fun waitForKeycloak() = waitForKeycloak
    override fun waitForKeycloakTimeout() = waitForKeycloakTimeout
    override fun failOnUndefinedVariables() = failOnUndefinedVariables
    override fun warnOnUndefinedVariables() = warnOnUndefinedVariables
    override fun disableSetUnmanagedAttributesToAdminEdit() = disableSetUnmanagedAttributesToAdminEdit
}
