import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "2.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.slf4j:slf4j-api:1.7.36")
    api("io.github.openfeign:feign-core:12.3")
    implementation("io.github.openfeign:feign-jackson:12.3")
    implementation("io.github.openfeign:feign-httpclient:12.3")
    implementation("io.github.openfeign.form:feign-form:3.8.0")
    implementation("io.github.resilience4j:resilience4j-feign:1.5.0")
    implementation("io.github.resilience4j:resilience4j-micrometer:1.5.0")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation("io.insert-koin:koin-core:3.2.2")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    testImplementation("org.slf4j:slf4j-api:1.7.36")
    testImplementation("org.apache.logging.log4j:log4j-core:2.20.0")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.9")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.insert-koin:koin-test:3.2.2")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

val sourcesJar by tasks.registering(Jar::class, fun Jar.() {
  dependsOn.add(tasks.javadoc)
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
})

val javadocJar by tasks.registering(Jar::class, fun Jar.() {
  dependsOn.add(tasks.javadoc)
  archiveClassifier.set("javadoc")
  from(tasks.javadoc)
})

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = "de.klg71.keycloakmigration"
            artifact(sourcesJar)
            artifact(javadocJar)
            from(components["java"])
        }
    }
    repositories {
        maven {
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                val ossrhUser = project.findProperty("ossrhUser") as String? ?: ""

                username = ossrhUser
                val ossrhPassword = project.findProperty("ossrhPassword") as String? ?: ""
                password = ossrhPassword
                if (ossrhUser.isBlank() || ossrhPassword.isBlank()) {
                    logger.warn("Sonatype user and password are not set you won't be able to publish to maven central!")
                }
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mayope/keycloakmigration")
            credentials {
                val githubUser = project.findProperty("githubPublishUser") as String? ?: ""
                username = githubUser
                val githubAccessToken = project.findProperty("githubPublishKey") as String? ?: ""
                password = githubAccessToken
                if (githubUser.isBlank() || githubAccessToken.isBlank()) {
                    logger.warn("Github user and password are not set you won't be able to publish to github!")
                }
            }
        }
    }
}

val publications = project.publishing.publications.withType(MavenPublication::class.java).map {
    with(it.pom) {
        withXml {
            val root = asNode()
            root.appendNode("name", "keycloakapi")
            root.appendNode("description", "Keycloak configuration as migration files")
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

signing {
    sign(publishing.publications["mavenJava"])
}



gradle.taskGraph.whenReady {
    if (allTasks.any { it is Sign }) {
        allprojects {
            extra["signing.keyId"] = "5357AC31"
            extra["signing.secretKeyRingFile"] = project.findProperty("signing_key_ring_file")
            extra["signing.password"] = project.findProperty("signing_key_ring_file_password")
        }
    }
}

tasks {
    dokkaHtml.configure {
        doFirst {
            System.setProperty("idea.io.use.fallback", "true")
        }
        outputDirectory.set(File("${rootProject.projectDir}/docsbuild/static/documentation"))
        dokkaSourceSets {
            configureEach {
                includes.setFrom(listOf("keycloakapi.md"))
            }
        }
    }

}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
  }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
