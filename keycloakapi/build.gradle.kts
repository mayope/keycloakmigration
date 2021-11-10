plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.dokka") version "0.10.1"
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.slf4j:slf4j-api:1.7.30")
    api("io.github.openfeign:feign-core:10.1.0")
    implementation("io.github.openfeign:feign-jackson:10.1.0")
    implementation("io.github.openfeign:feign-httpclient:10.1.0")
    implementation("io.github.openfeign.form:feign-form:3.4.1")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3")
    implementation("io.insert-koin:koin-core:3.1.3")
    implementation("commons-codec:commons-codec:1.11")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    implementation("org.apache.commons:commons-text:1.8")
    implementation("org.apache.commons:commons-lang3:3.9")

    testImplementation("org.slf4j:slf4j-api:1.7.30")
    testImplementation("org.apache.logging.log4j:log4j-core:2.11.1")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.11.1")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.9")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("io.insert-koin:koin-test:3.1.3")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    testImplementation("org.assertj:assertj-core:3.11.1")
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
                    org.jetbrains.kotlin.org.jline.utils.Log.warn(
                            "Sonatype user and password are not set you won't be able to publish to maven central!")
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


tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
