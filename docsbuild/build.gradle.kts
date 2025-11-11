import org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
import org.apache.tools.ant.taskdefs.condition.Os.isFamily

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

tasks {

    /*
    register("buildDocs") {
        dependsOn(":keycloakapi:dokkaGenerateHtml")
        doLast {
            if (isFamily(FAMILY_WINDOWS)) {
                providers.exec {
                    workingDir(project.projectDir)
                    commandLine("${project.projectDir}\\bin\\hugo.exe", "-d", "..\\docs")
                }
            } else {
                providers.exec {
                    workingDir(project.projectDir)
                    commandLine("bin/hugo", "-d", "../docs")
                }
            }
        }
    }
     */
}

