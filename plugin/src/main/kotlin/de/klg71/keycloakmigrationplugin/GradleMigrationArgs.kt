package de.klg71.keycloakmigrationplugin

import de.klg71.keycloakmigration.MigrationArgs

open class GradleMigrationArgs(private val adminUser: String, private val adminPassword: String,
                               private val migrationFile: String, private val baseUrl: String,
                               private val realm: String, private val clientId: String,
                               private val correctHashes: Boolean,
                               private val parameters: Map<String, String>,
                               private val waitForKeycloak: Boolean,
                               private val waitForKeycloakTimeout: Long,
                               private val failOnUndefinedVariables: Boolean,
                               private val warnOnUndefinedVariables: Boolean
) : MigrationArgs {
    override fun adminUser() = adminUser
    override fun adminPassword() = adminPassword
    override fun baseUrl() = baseUrl
    override fun migrationFile() = migrationFile
    override fun parameters() = parameters

    override fun realm() = realm
    override fun clientId() = clientId
    override fun correctHashes() = correctHashes
    override fun waitForKeycloak() = waitForKeycloak
    override fun waitForKeycloakTimeout() = waitForKeycloakTimeout
    override fun failOnUndefinedVariables() = failOnUndefinedVariables
    override fun warnOnUndefinedVariables() = warnOnUndefinedVariables
}
