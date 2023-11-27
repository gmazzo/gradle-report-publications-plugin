plugins {
    id("io.github.gmazzo.gradle.publications.report")
    `maven-publish`
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    group = "io.gmazzo.demo.build-logic"
    version = "0.1.0"

    publishing {
        publications.create<MavenPublication>("java") { from(components["java"]) }
        repositories {
            maven {
                name = "local"
                url = uri(rootProject.layout.buildDirectory.dir("repo"))
            }
        }
    }
}
