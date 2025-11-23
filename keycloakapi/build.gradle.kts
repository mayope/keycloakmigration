import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("signing")

    id ("org.danilopianini.publish-on-central")

    id("dokka-convention")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.slf4j:slf4j-api:2.0.16")
    api("io.github.openfeign:feign-core:13.5")
    implementation("io.github.openfeign:feign-jackson:13.5")
    implementation("io.github.openfeign:feign-httpclient:13.5")
    implementation("io.github.openfeign.form:feign-form:3.8.0")
    implementation("io.github.resilience4j:resilience4j-feign:2.3.0")
    implementation("io.github.resilience4j:resilience4j-retry:2.3.0")
    implementation("io.github.resilience4j:resilience4j-micrometer:2.3.0")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.3.0")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("io.insert-koin:koin-core:4.0.2")
    implementation("commons-codec:commons-codec:1.18.0")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    implementation("org.apache.commons:commons-text:1.13.0")
    implementation("org.apache.commons:commons-lang3:3.17.0")

    testImplementation("org.slf4j:slf4j-api:2.0.16")
    testImplementation("org.apache.logging.log4j:log4j-core:2.24.3")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("io.insert-koin:koin-test:4.0.2")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.assertj:assertj-core:3.27.3")
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        id.set("klg71")
                        name.set("Lukas Meisegeier")
                        email.set("MeisegeierLukas@gmx.de")
                    }
                }
            }
            repositories {
                maven {
                    setUrl("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                    credentials {
                        val ossrhUser = project.findProperty("ossrhUser") as String? ?: ""
                        username = ossrhUser
                        val ossrhPassword = project.findProperty("ossrhPassword") as String? ?: ""
                        password = ossrhPassword
                        if (ossrhUser.isBlank() || ossrhPassword.isBlank()) {
                            logger.warn(
                                "Sonatype user and password are not set you won't be able to publish to maven central!"
                            )
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
    }
}

group = "de.klg71.keycloakmigration"
publishOnCentral {
    repoOwner.set("klg71")
    projectDescription.set("Alternative Keycloak Api")
    projectLongName.set(project.name)
    licenseName.set("MIT License")
    licenseUrl.set("https://github.com/mayope/keycloakmigration/blob/master/LICENSE.md")
    projectUrl.set("https://github.com/mayope/keycloakmigration")
    scmConnection.set("scm:git:ssh://git@github.com/mayope/keycloakmigration.git")
}

signing {
    sign(publishing.publications["OSSRH"])
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


tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_23)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}
