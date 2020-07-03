package de.klg71.keycloakmigrationplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

open class KeycloakMigrationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create<KeycloakMigrationTask>("keycloakMigrate", KeycloakMigrationTask::class.java).run {
            description = "Execute keycloak migrations"
            group = "keycloakmigration"
        }
        project.tasks.create<KeycloakMigrationCorrectHashesTask>("keycloakMigrateCorrectHashes",
                KeycloakMigrationCorrectHashesTask::class.java).run {
            description = "Execute keycloak migrations and correct hashes. Dont use this task in build pipelines! See Readme.md for more information."
            group = "keycloakmigration"
        }
    }
}

