import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "0.10.1"

    id("com.github.johnrengelman.shadow")
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")

    compile("io.github.openfeign:feign-core:10.1.0")
    compile("io.github.openfeign:feign-jackson:10.1.0")
    compile("io.github.openfeign:feign-slf4j:10.1.0")
    compile("io.github.openfeign:feign-httpclient:10.1.0")
    compile("io.github.openfeign.form:feign-form:3.4.1")

    compile("org.apache.logging.log4j:log4j-core:2.11.1")
    compile("org.apache.logging.log4j:log4j-slf4j-impl:2.11.1")

    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.3")
    compile("com.fasterxml.jackson.core:jackson-databind:2.10.3")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    compile("org.koin:koin-core:2.0.1")
    compile("commons-codec:commons-codec:1.11")
    compile("com.xenomachina:kotlin-argparser:2.0.7")

    compile("org.apache.commons:commons-text:1.8")
    compile("org.apache.commons:commons-lang3:3.9")

    testCompile(kotlin("test"))
    testCompile(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.9")
    testCompile("org.assertj:assertj-core:3.11.1")
    testCompile("org.koin:koin-test:2.0.1")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    testCompile("org.assertj:assertj-core:3.11.1")
}

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn.add(tasks.javadoc)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn.add(tasks.javadoc)
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

val fatJar by tasks.named("shadowJar")
publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            groupId = "de.klg71.keycloakmigration"
            artifact(sourcesJar)
            artifact(javadocJar)
            artifact(fatJar)
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
                    org.jetbrains.kotlin.org.jline.utils.Log.warn(
                            "Sonatype user and password are not set you won't be able to publish to maven central!")
                }
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/klg71/keycloakmigration")
            credentials {
                val githubUser = project.findProperty("githubPublishUser") as String? ?: ""
                username = githubUser
                val githubAccessToken = project.findProperty("githubPublishKey") as String? ?: ""
                password = githubAccessToken
                if (githubUser.isBlank() || githubAccessToken.isBlank()) {
                    org.jetbrains.kotlin.org.jline.utils.Log.warn(
                            "Github user and password are not set you won't be able to publish to github!")
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
            root.appendNode("url", "https://github.com/klg71/keycloakmigration")
        }
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/klg71/keycloakmigration")
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
            connection.set("scm:git:git://github.com/klg71/keycloakmigration.git")
            developerConnection.set("scm:git:ssh://git@github.com/klg71/keycloakmigration.git")
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
    named<ShadowJar>("shadowJar") {
        classifier = "fat"
    }

    register<org.jetbrains.dokka.gradle.DokkaTask>("documentation") {
        doFirst {
            System.setProperty("idea.io.use.fallback", "true")
        }
        outputFormat = "html"
        outputDirectory = "${rootProject.projectDir}/docsbuild/static/documentation"
        configuration {
            includes = listOf("keycloakapi.md")
        }
    }
}
