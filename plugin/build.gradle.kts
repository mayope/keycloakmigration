plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "2.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api(rootProject)

    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    implementation(gradleApi())
    implementation(localGroovy())
}

gradlePlugin {
    website = "https://mayope.net"
    vcsUrl = "https://github.com/mayope/keycloakmigration"

    plugins {
        create("keycloakmigrationplugin") {
            id = "de.klg71.keycloakmigrationplugin"
            implementationClass = "de.klg71.keycloakmigrationplugin.KeycloakMigrationPlugin"
            displayName = "keycloakmigration"
            description = "Plugin to provide liquibase like migrations for keycloak"
            tags = listOf("keycloak", "migration")
        }
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}

val publications = project.publishing.publications.withType(MavenPublication::class.java).map {
    with(it.pom) {
        withXml {
            val root = asNode()
            root.appendNode("name", "keycloakmigrationplugin")
            root.appendNode("description", "Keycloak configuration as migration files, gradle plugin")
            root.appendNode("url", "https://github.com/mayope/keycloakmigration")
        }
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/mayope/keycloakmigration")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("klg71")
                name.set("Lukas Meisegeier")
                email.set("MeisegeierLukas@gmx.de")
            }
        }
        scm {
            url.set("https://github.com/mayope/keycloakmigration")
            connection.set("scm:git:git://github.com/mayope/keycloakmigration.git")
            developerConnection.set("scm:git:ssh://git@github.com/mayope/keycloakmigration.git")
        }
    }
}

signing{
    sign(publishing.publications["mavenJava"])
}
