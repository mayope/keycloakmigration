import org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
import org.apache.tools.ant.taskdefs.condition.Os.isFamily
import java.nio.file.Paths

plugins {
    kotlin("jvm")
    id("dokka-convention")
}

repositories {
    mavenCentral()
}

dependencies{
    dokka(project(":keycloakapi"))
    dokka(project(":"))
}
tasks {
    named("build"){
        dependsOn("buildDocs")
    }

    register("buildDocs") {
        dependsOn(":docsbuild:dokkaGenerateHtml")
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
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(File(rootDir.path+("/docs/documentation"))) } }

