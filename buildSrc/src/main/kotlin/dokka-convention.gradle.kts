plugins {
    id("org.jetbrains.dokka")
}

dokka {
    dokkaPublications.html {
        //outputDirectory.set(rootDir.resolve("docs/documentation/"))
        //includes.from(project.layout.projectDirectory.file("README.md"))
        //includes.from(project.layout.projectDirectory.file("keycloakapi/keycloakapi.md"))
    }

    //moduleName.set("")
    /*
    dokkaPublications.html {
        suppressInheritedMembers.set(true)
        failOnWarning.set(true)
    }
    dokkaSourceSets.named("main") {
        includes.from("README.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
        }
    }
     */
}