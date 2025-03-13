import org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
import org.apache.tools.ant.taskdefs.condition.Os.isFamily

plugins {
    kotlin("jvm")
}

repositories {
    jcenter()
}

tasks {

    register("buildDocs") {
        dependsOn(":keycloakapi:dokkaHtml")
        doLast {
            if (isFamily(FAMILY_WINDOWS)) {
                project.exec {
                    workingDir(project.projectDir)
                    commandLine("${project.projectDir}\\bin\\hugo.exe", "-d", "..\\docs")
                }
            } else {
                project.exec {
                    workingDir(project.projectDir)
                    commandLine("bin/hugo", "-d", "../docs")
                }
            }
        }
    }
}

