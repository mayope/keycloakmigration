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
    website = "https://www.klg71.de"
    vcsUrl = "https://github.com/klg71/keycloakmigrationplugin"
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
            root.appendNode("url", "https://github.com/klg71/keycloakmigrationplugin")
        }
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/klg71/keycloakmigrationplugin")
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
            url.set("https://github.com/klg71/keycloakmigration")
            connection.set("scm:git:git://github.com/klg71/keycloakmigrationplugin.git")
            developerConnection.set("scm:git:ssh://git@github.com/klg71/keycloakmigrationplugin.git")
        }
    }
}

signing{
    sign(publishing.publications["mavenJava"])
}
