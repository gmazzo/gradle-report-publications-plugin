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
                name = "myRepo"
                url = uri(rootProject.layout.buildDirectory.dir("repo"))
            }
        }
    }
}

tasks.publish {
    subprojects { this@publish.dependsOn(tasks.publish) }
}

tasks.publishToMavenLocal {
    subprojects { this@publishToMavenLocal.dependsOn(tasks.publishToMavenLocal) }
}
