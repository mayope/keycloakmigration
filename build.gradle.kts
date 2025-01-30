import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import de.undercouch.gradle.tasks.download.Download
import org.apache.tools.ant.taskdefs.condition.Os
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
    kotlin("jvm") version "1.6.21"
    id("maven-publish")
    id("signing")
    id("de.undercouch.download") version ("3.4.3")
    id("net.researchgate.release") version ("2.8.0")

    // Security check for dependencies by task
    id("org.owasp.dependencycheck") version "5.3.0"
    // static code analysis
    id("io.gitlab.arturbosch.detekt") version "1.19.0-RC1"

    id("com.github.johnrengelman.shadow") version "6.0.0" apply (false)
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project("keycloakapi"))

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation("io.insert-koin:koin-core:3.2.2")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")

    // Commons
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    // Logging
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")

    testImplementation("io.github.openfeign:feign-slf4j:12.3")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.9")

    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.insert-koin:koin-test:3.2.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
}


repositories {
    mavenCentral()
}

tasks {
    val keycloakVersion = "26.0.7"

    named("build") {
        dependsOn("buildDocker", ":docsbuild:buildDocs")
    }

    register<ShadowJar>("shadowJar") {
        archiveClassifier.set("fat")
        manifest {
            attributes["Main-Class"] = "de.klg71.keycloakmigration.MainKt"
        }
        from(sourceSets.main.get().output)
        from(project(":keycloakapi").sourceSets.main.get().output)
        configurations = mutableListOf(
            project.configurations.compileClasspath.get(),
            project.configurations.runtimeClasspath.get()
        )
        project.configurations.compileClasspath.allDependencies.forEach {
            println(it)
        }
    }


    "afterReleaseBuild"{
        dependsOn(
            "publishMavenJavaPublicationToMavenRepository",
            "publishMavenJavaPublicationToGitHubPackagesRepository",
            "plugin:publishPlugins",
            "keycloakapi:publishMavenJavaPublicationToMavenRepository",
            "keycloakapi:publishMavenJavaPublicationToGitHubPackagesRepository",
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
        into("keycloak")
    }
    register("startLocalKeycloak") {
        group = "keycloakmigration"
        description = "Starts local keycloak"

        if (!File("keycloak/keycloak-$keycloakVersion").exists()) {
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
            ProcessBuilder(
                "cmd", "/c", "kc.bat", "start-dev", "--http-port=18080", "--http-management-port=18081", "--hostname-strict=false","--http-relative-path=/auth","--log-level=info", ">",
                "output.txt"
            ).run {
                directory(File("keycloak/keycloak-$keycloakVersion/bin"))
                println("Starting local Keycloak on windows")
                environment()["NOPAUSE"] = "true"
                environment()["KEYCLOAK_ADMIN"] = "admin"
                environment()["KEYCLOAK_ADMIN_PASSWORD"] = "admin"
                start()
                waitForKeycloak()
            }
        }
    }
    register("execLinuxKeycloak") {
        doLast {
            ProcessBuilder(
                "keycloak/keycloak-$keycloakVersion/bin/standalone.sh",
                "-Djboss.socket.binding.port-offset=10000"
            ).run {
                println("Starting local Keycloak on linux")
                environment()["KEYCLOAK_ADMIN"] = "admin"
                environment()["KEYCLOAK_ADMIN_PASSWORD"] = "admin"
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
            val pid = command(listOf("netstat", "-aon")).lines().firstOrNull {
                it.contains("TCP") && it.contains("0.0.0.0:18080") && it.contains("LISTENING")
            }?.split(" ")?.last()?.toLong()
            if (pid != null) {
                println("Killing process: $pid")
                command(listOf("taskkill", "/F", "/PID", pid.toString()))
            }
        }
    }

    register<Exec>("stopLinuxKeycloak") {
        workingDir("keycloak/keycloak-$keycloakVersion/bin")
        commandLine("sh", "jboss-cli.sh", "--connect", "--command=:shutdown", "--controller=127.0.0.1:19990")
        standardOutput = System.out
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
    val version = project.version
    val tag = "$tagName:$version"
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
                    "--label","\"org.opencontainers.image.version=$version\"",
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
            root.appendNode("name", "keycloakmigration")
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

dependencyCheck {
    failOnError = true
    // https://www.first.org/cvss/specification-document#Qualitative-Severity-Rating-Scale
    failBuildOnCVSS = 0.0f
    analyzers.assemblyEnabled = false
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
