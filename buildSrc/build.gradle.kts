plugins {

    id("org.gradle.kotlin.kotlin-dsl") version "6.4.2"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0-Beta2")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.1.0")
}