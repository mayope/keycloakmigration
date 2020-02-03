import de.undercouch.gradle.tasks.download.Download
import groovy.lang.GroovyObject
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.org.jline.utils.Log
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig
import java.net.ConnectException

plugins {
    kotlin("jvm") version "1.3.0"
    id("maven-publish")
    id("signing")
    id("com.jfrog.artifactory") version "4.8.1"
    id("de.undercouch.download") version ("3.4.3")
    id("net.researchgate.release") version ("2.8.0")
}

tasks.withType<Wrapper> {
    gradleVersion = "5.1"
}

dependencies {
    compile(kotlin("stdlib"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")

    compile("io.github.openfeign:feign-core:10.1.0")
    compile("io.github.openfeign:feign-jackson:10.1.0")
    compile("io.github.openfeign:feign-slf4j:10.1.0")
    compile("io.github.openfeign.form:feign-form:3.4.1")

    compile("org.apache.logging.log4j:log4j-core:2.11.1")
    compile("org.apache.logging.log4j:log4j-slf4j-impl:2.11.1")

    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.8")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    compile("org.koin:koin-core:2.0.1")
    compile("commons-codec:commons-codec:1.11")
    compile("com.xenomachina:kotlin-argparser:2.0.7")
    compile("org.apache.commons:commons-text:1.8")

    testCompile(kotlin("test"))
    testCompile(kotlin("test-junit"))
    testCompile("org.assertj:assertj-core:3.11.1")
    testCompile("org.koin:koin-test:2.0.1")
    testCompile("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    testCompile("org.assertj:assertj-core:3.11.1")
}


repositories {
    jcenter()
}

artifactory {
    setContextUrl("https://artifactory.klg71.de/artifactory")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<GroovyObject> {
            setProperty("repoKey", "keycloakmigration")
            setProperty("username", project.findProperty("artifactory_user"))
            setProperty("password", project.findProperty("artifactory_password"))
            setProperty("maven", true)

        })
        defaults(delegateClosureOf<GroovyObject> {
            invokeMethod("publications", "mavenJava")
        })
    })
    resolve(delegateClosureOf<ResolverConfig> {
        setProperty("repoKey", "libs-release")
    })
}

tasks {

    val keycloakVersion = "8.0.1"

    register<Jar>("fatJar") {
        from(sourceSets["main"].output)
        classifier = "fat"
        manifest {
            attributes["Main-Class"] = "de.klg71.keycloakmigration.MainKt"
        }

        from(configurations["runtimeClasspath"].map { if (it.isDirectory) it else zipTree(it) })
    }

    "afterReleaseBuild"{
        dependsOn("artifactoryPublish")
        dependsOn("publishMavenJavaPublicationToMavenRepository")
        dependsOn("publishMavenJavaPublicationToGitHubPackagesRepository")
    }


    register<Download>("downloadKeycloak") {
        description = "Download local keycloak distribution for testing purposes"
        src("https://downloads.jboss.org/keycloak/$keycloakVersion/keycloak-$keycloakVersion.zip")
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
        dependsOn("addWindowsAdminUser")
        finalizedBy("execWindowsKeycloak")
    }
    register("startLinuxKeycloak") {
        dependsOn("addLinuxAdminUser")
        finalizedBy("execLinuxKeycloak")
    }
    register<Exec>("addLinuxAdminUser") {
        workingDir("keycloak/keycloak-$keycloakVersion/bin")
        commandLine("sh", "add-user-keycloak.sh", "-r", "master", "-u", "admin", "-p", "admin")
        isIgnoreExitValue = true
        standardOutput = System.out
    }

    register<Exec>("addWindowsAdminUser") {
        workingDir("keycloak/keycloak-$keycloakVersion/bin")
        commandLine("cmd", "/c", "add-user-keycloak.bat", "-r", "master", "-u", "admin", "-p", "admin")
        environment("JAVA_OPTS", "--add-modules=java.se")
        environment("NOPAUSE", "true")
        isIgnoreExitValue = true
        standardOutput = System.out
    }

    fun waitForKeycloak() {
        while (true) {
            try {
                if (uri("http://localhost:18080/auth/").toURL().readBytes().isNotEmpty())
                    return
            } catch (e: ConnectException) {
            }
            println("Waiting for Keycloak to become ready")
            Thread.sleep(1000)
        }
    }

    register("execWindowsKeycloak") {
        doLast {
            ProcessBuilder("cmd", "/c", "standalone.bat", "-Djboss.socket.binding.port-offset=10000", ">",
                    "output.txt").run {
                directory(File("keycloak/keycloak-$keycloakVersion/bin"))
                println("Starting local Keycloak on windows")
                environment()["NOPAUSE"] = "true"
                start()
                waitForKeycloak()
            }
        }
    }
    register("execLinuxKeycloak") {
        doLast {
            ProcessBuilder("keycloak/keycloak-$keycloakVersion/bin/standalone.sh",
                    "-Djboss.socket.binding.port-offset=10000").run {
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
    register<Exec>("stopWindowsKeycloak") {
        // Use jboss cli to shutdown local server
        workingDir("keycloak/keycloak-$keycloakVersion/bin")
        commandLine("cmd", "/c", "jboss-cli.bat", "--connect", "--command=:shutdown", "--controller=127.0.0.1:19990")
        environment("NOPAUSE", "true")
        standardOutput = System.out
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

val fatJar by tasks.named("fatJar")

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
                    Log.warn("Sonatype user and password are not set you won't be able to publish!")
                }
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/klg71/keycloakmigration")
            credentials {
                username = project.findProperty("githubPublishUser") as String? ?: ""
                password = project.findProperty("githubPublishKey") as String? ?: ""
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

