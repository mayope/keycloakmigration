import groovy.lang.GroovyObject
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.dsl.ResolverConfig

plugins {
    kotlin("jvm") version "1.3.0"
    id("maven-publish")
    id("com.jfrog.artifactory") version "4.8.1"
}


dependencies {
    compile(kotlin("stdlib"))
    compile("io.github.openfeign:feign-core:10.1.0")
    compile("io.github.openfeign:feign-jackson:10.1.0")
    compile("io.github.openfeign:feign-slf4j:10.1.0")
    compile("io.github.openfeign.form:feign-form:3.4.1")

    compile("org.apache.logging.log4j:log4j-core:2.11.1")
    compile("org.apache.logging.log4j:log4j-slf4j-impl:2.11.1")

    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.7")
    compile("com.fasterxml.jackson.core:jackson-databind:2.9.7")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")
    compile("org.koin:koin-core:1.0.2")
    compile("commons-codec:commons-codec:1.11")
}

repositories {
    jcenter()
    maven {
        setUrl("https://artifactory.klg71.de/artifactory/libs-releases")
        credentials {
            username = project.findProperty("artifactory_user") as String
            password = project.findProperty("artifactory_password") as String
        }
    }
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
}}

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
