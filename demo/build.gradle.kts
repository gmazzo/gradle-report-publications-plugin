plugins {
    id("io.github.gmazzo.publications.report")
    `maven-publish`
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    group = "io.gmazzo.demo"
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

val buildLogic = gradle.includedBuild("build-logic")
tasks.publish { dependsOn(buildLogic.task(":$name")) }
tasks.publishToMavenLocal { dependsOn(buildLogic.task(":$name")) }
