plugins {
    id("io.github.gmazzo.publications.report")
    `ivy-publish`
    `maven-publish`
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "ivy-publish")
    apply(plugin = "maven-publish")

    group = "io.gmazzo.demo"
    version = "0.1.0"

    publishing {
        publications {
            create<MavenPublication>("javaMaven") { from(components["java"]) }
            create<IvyPublication>("javaIvy") { from(components["java"]) }
        }
        repositories {
            val repos = rootProject.layout.buildDirectory.dir("repo")

            maven(url = repos.map { it.dir("maven").asFile.toURI() }) { name = "localMaven" }
            ivy(url = repos.map { it.dir("ivy").asFile.toURI() }) { name = "localIvy" }
        }
    }
}

tasks {
    val buildLogic = gradle.includedBuild("build-logic")

    sequenceOf(
        publish,
        publishToMavenLocal,
        named("publishAllPublicationsToLocalMavenRepository"),
        named("publishAllPublicationsToLocalIvyRepository"),
    ).forEach { it.configure task@{ subprojects { this@task.dependsOn(buildLogic.task(":${this@task.name}")) } } }
}
