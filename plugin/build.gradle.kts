plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "0.11.0"
     `maven-publish`
    id("signing")
    id("java-gradle-plugin")
}

repositories {
    jcenter()
}

dependencies {
    api(project(":"))

    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    implementation(gradleApi())
    implementation(localGroovy())
}

pluginBundle {
    website = "https://mayope.net"
    vcsUrl = "https://github.com/mayope/keycloakmigration"
    tags = listOf("keycloak", "migration")

    plugins {
        create("keycloakmigrationplugin") {
            id = "de.klg71.keycloakmigrationplugin"
            displayName = "keycloakmigration"
            description = "Plugin to provide liquibase like migrations for keycloak"
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
