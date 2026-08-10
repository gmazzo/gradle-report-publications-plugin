plugins {
    id("demo-module-convention")
    signing
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
