import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.undercouch.gradle.tasks.download.Download
import net.researchgate.release.ReleaseExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream
import java.net.ConnectException

fun Project.command(cmd: List<String>, workingDirectory: String = ".", environment: Map<String, String> = emptyMap()) =
    ByteArrayOutputStream().also { stream ->
        logger.info("Running command $cmd")
        exec {
            environment(environment)
            commandLine = cmd
            standardOutput = stream
            workingDir = File(workingDirectory)
        }
    }.run {
        toString().trim()
    }

plugins {
    kotlin("jvm") version "2.1.10"
    id("maven-publish")
    id("signing")
    id("de.undercouch.download") version "5.6.0"
    id("net.researchgate.release") version "3.1.0"

    // Security check for dependencies by task
    id("org.owasp.dependencycheck") version "12.0.1"
    // static code analysis
    id("io.gitlab.arturbosch.detekt") version "1.23.7"

    id("com.github.johnrengelman.shadow") version "8.1.1" apply (false)
    id ("org.danilopianini.publish-on-central") version "9.1.0"

    id("org.jetbrains.dokka") version "2.0.0"
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project("keycloakapi"))

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("io.insert-koin:koin-core:4.0.2")
    implementation("commons-codec:commons-codec:1.18.0")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    // Commons
    implementation("org.apache.commons:commons-text:1.13.0")
    implementation("org.apache.commons:commons-lang3:3.17.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("io.insert-koin:koin-logger-slf4j:4.0.2")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")

    testImplementation("io.github.openfeign:feign-slf4j:13.5")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.13.16")

    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("io.insert-koin:koin-test:4.0.2")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("com.github.tomakehurst:wiremock-jre8:3.0.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}

repositories {
    mavenCentral()
}

tasks {
    val keycloakVersion = "26.3.2"

    named("build") {
        dependsOn("buildDocker", "docsbuild:buildDocs")
    }

    register<ShadowJar>("shadowJar") {
        archiveClassifier.set("fat")
        manifest {
            attributes["Main-Class"] = "de.klg71.keycloakmigration.MainKt"
        }
        from(sourceSets.main.get().output)
        from(project("keycloakapi").sourceSets.main.get().output)
        configurations = listOf(
            project.configurations.compileClasspath.get(),
            project.configurations.runtimeClasspath.get()
        )
        project.configurations.compileClasspath.get().allDependencies.forEach {
            println(it)
        }
    }


    "afterReleaseBuild"{
        dependsOn(
            "publishAllPublicationsToProjectLocalRepository",
            "zipMavenCentralPortalPublication",
            "validateMavenCentralPortalPublication",
            "releaseMavenCentralPortalPublication",
            //"publishMavenJavaPublicationToGitHubPackagesRepository",
            "plugin:publishPlugins",
            //"keycloakapi:publishMavenJavaPublicationToMavenRepository",
            //"keycloakapi:publishMavenJavaPublicationToGitHubPackagesRepository",
            "pushDocker", "shadowJar"
        )
    }


    register<Download>("downloadKeycloak") {
        description = "Download local keycloak distribution for testing purposes"
        src("https://github.com/keycloak/keycloak/releases/download/$keycloakVersion/keycloak-$keycloakVersion.zip")
        dest("$buildDir/keycloak-$keycloakVersion.zip")
        overwrite(false)
    }
    register<Copy>("setupKeycloak") {
        dependsOn("downloadKeycloak")
        description = "Setup local keycloak for testing purposes"

        from(zipTree("$buildDir/keycloak-$keycloakVersion.zip"))
        into("$buildDir/keycloak")
    }
    register("startLocalKeycloak") {
        group = "keycloakmigration"
        description = "Starts local keycloak"

        if (!File(project.buildDir, "keycloak/keycloak-$keycloakVersion").exists()) {
            dependsOn("setupKeycloak")
        }

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            finalizedBy("startWindowsKeycloak")
        } else {
            finalizedBy("startLinuxKeycloak")
        }
    }

    register("startWindowsKeycloak") {
        finalizedBy("execWindowsKeycloak")
    }
    register("startLinuxKeycloak") {
        finalizedBy("execLinuxKeycloak")
    }

    fun waitForKeycloak() {
        while (true) {
            try {
                if (uri("http://localhost:18080/auth").toURL().readBytes().isNotEmpty())
                    return
            } catch (e: ConnectException) {
            }
            println("Waiting for Keycloak to become ready")
            Thread.sleep(1000)
        }
    }

    register("execWindowsKeycloak") {
        doLast {
            val keycloakDir = File(project.buildDir, "keycloak/keycloak-$keycloakVersion/bin")
            val outputFile = File(project.buildDir, "keycloak/output.txt")

            ProcessBuilder(
                "cmd", "/c", "kc.bat", "start-dev", "--http-port=18080", "--http-management-port=18081", "--hostname-strict=false","--http-relative-path=/auth","--log-level=info", ">",
                "${outputFile}"
            ).run {
                directory(keycloakDir)

                environment()["NOPAUSE"] = "true"
                environment()["KEYCLOAK_ADMIN"] = "admin"
                environment()["KEYCLOAK_ADMIN_PASSWORD"] = "admin"

                println("Starting local Keycloak on windows")
                start()

                waitForKeycloak()
            }
        }
    }
    register("execLinuxKeycloak") {
        doLast {
            val keycloakDir = File(project.buildDir, "keycloak/keycloak-$keycloakVersion/bin")
            val outputFile = File(project.buildDir, "keycloak/output.txt")

            ProcessBuilder(
                "./kc.sh", "start-dev", "--http-port=18080", "--http-management-port=18081", "--hostname-strict=false", "--http-relative-path=/auth", "--log-level=info"
            ).apply {
                directory(keycloakDir)

                environment()["KEYCLOAK_ADMIN"] = "admin"
                environment()["KEYCLOAK_ADMIN_PASSWORD"] = "admin"

                redirectOutput(outputFile)
                redirectErrorStream(true)

                println("Starting local Keycloak on linux")
                start()

                waitForKeycloak()
            }
        }
    }
    register("stopLocalKeycloak") {
        group = "keycloakmigration"
        description = "Stops local keycloak"
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            finalizedBy("stopWindowsKeycloak")
        } else {
            finalizedBy("stopLinuxKeycloak")
        }
    }
    register("stopWindowsKeycloak") {
        doFirst {
            val port = 18080
            val pid = command(listOf("netstat", "-aon")).lines().firstOrNull {
                it.contains("TCP") && it.contains("0.0.0.0:$port") && it.contains("LISTENING")
            }?.split(" ")?.last()?.toLong()
            if (pid != null) {
                println("Killing process on port $port with PID $pid")
                command(listOf("taskkill", "/F", "/PID", pid.toString()))
            }
        }
    }

    register("stopLinuxKeycloak") {
        doFirst {
            val port = 18080
            val pid = try {
                ProcessBuilder("sh", "-c", "ss -lptn 'sport = :$port' | grep -o 'pid=[0-9]*' | cut -d= -f2")
                    .redirectErrorStream(true)
                    .start()
                    .inputStream
                    .bufferedReader()
                    .readText()
                    .trim()
            } catch (e: Exception) {
                null
            }
            if (!pid.isNullOrEmpty()) {
                println("Killing process on port $port with PID $pid")
                command(listOf("kill", "-9", pid))
            } else {
                println("No process found on port $port")
            }
        }
    }

    "test"{
        dependsOn("startLocalKeycloak")
        finalizedBy("stopLocalKeycloak")
    }

    withType<io.gitlab.arturbosch.detekt.Detekt> {
        // Target version of the generated JVM bytecode. It is used for type resolution.
        this.jvmTarget = "1.8"
    }
    val dockerBuildWorkingDirectory = "${project.buildDir}/buildDocker"
    val fatJar by named("shadowJar")
    register<Copy>("prepareDocker") {
        dependsOn("shadowJar")

        from(fatJar.outputs.files) {
            include("*.*")
        }
        from("src/docker") {
            include("*")
        }
        into(dockerBuildWorkingDirectory)
    }

    val tagName = "klg71/keycloakmigration"
    val projectVersion = project.version
    val tag = "$tagName:$projectVersion"
    val tagLatest = "$tagName:latest"
    register("buildDocker") {
        dependsOn("prepareDocker")
        doLast {
            exec {
                workingDir(dockerBuildWorkingDirectory)
                commandLine(
                    "docker", "build", ".", "-t", tag, "--build-arg",
                    "jar_file=${fatJar.outputs.files.first().name}",
                    "--label","\"org.opencontainers.image.url=https://github.com/mayope/keycloakmigration.git\"",
                    "--label","\"org.opencontainers.image.source=https://github.com/mayope/keycloakmigration.git\"",
                    "--label","\"org.opencontainers.image.version=$projectVersion\"",
                )
            }
            exec {
                commandLine("docker", "tag", tag, tagLatest)
            }
        }
    }
    register("pushDocker") {
        dependsOn("buildDocker")
        doLast {
            exec {
                commandLine("docker", "push", tag)
            }
            exec {
                commandLine("docker", "push", tagLatest)
            }
        }
    }
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
    projectDescription.set("Keycloak configuration as migration files")
    projectLongName.set(project.name)
    licenseName.set("MIT License")
    licenseUrl.set("https://github.com/mayope/keycloakmigration/blob/master/LICENSE.md")
    projectUrl.set("https://github.com/mayope/keycloakmigration")
    scmConnection.set("scm:git:ssh://git@github.com/mayope/keycloakmigration.git")
}


signing {
    sign(publishing.publications["OSSRH"])
}
configure<ReleaseExtension>{
    with(git){
        requireBranch.set("master")
    }
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

dependencyCheck {
    failOnError = true
    // https://www.first.org/cvss/specification-document#Qualitative-Severity-Rating-Scale
    failBuildOnCVSS = 0.0f
    analyzers.assemblyEnabled = false
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
