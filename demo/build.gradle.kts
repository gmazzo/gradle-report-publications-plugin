plugins {
    id("io.github.gmazzo.publications.report")
    java
    `ivy-publish`
    `maven-publish`
    signing
}

val javaVersion = JavaLanguageVersion.of(libs.versions.java.get())
val singingKeyFile = providers.fileContents(layout.projectDirectory.file("singingkey.asc")).asText

allprojects {
    apply(plugin = "java")
    apply(plugin = "ivy-publish")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    group = "io.gmazzo.demo"
    version = "0.1.0"
    java.toolchain.languageVersion = javaVersion

    signing {
        useInMemoryPgpKeys(singingKeyFile.get(), "org.demo")
        sign(publishing.publications)
    }

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

    // CC issue with Ivy publication tasks
    tasks.withType<PublishToIvyRepository>().configureEach { mustRunAfter(tasks.withType<Sign>()) }
    tasks.withType<AbstractPublishToMaven>().configureEach { mustRunAfter(tasks.withType<Sign>()) }
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
