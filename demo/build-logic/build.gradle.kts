plugins {
    id("io.github.gmazzo.publications.report")
    java
    `ivy-publish`
    `maven-publish`
}

val javaVersion = JavaLanguageVersion.of(libs.versions.java.get())

allprojects {
    apply(plugin = "java")
    apply(plugin = "ivy-publish")
    apply(plugin = "maven-publish")

    group = "io.gmazzo.demo.build-logic"
    version = "0.1.0"
    java.toolchain.languageVersion = javaVersion

    publishing {
        publications {
            create<MavenPublication>("javaMaven") { from(components["java"]) }
            create<IvyPublication>("javaIvy") { from(components["java"]) }
        }
        repositories {
            val repos = gradle.parent!!.rootProject.layout.buildDirectory.dir("repo")

            maven(url = repos.map { it.dir("maven").asFile.toURI() }) { name = "localMaven" }
            ivy(url = repos.map { it.dir("ivy").asFile.toURI() }) { name = "localIvy" }
        }
    }
}

tasks.publish {
    subprojects { this@publish.dependsOn(tasks.publish) }
}

tasks.publishToMavenLocal {
    subprojects { this@publishToMavenLocal.dependsOn(tasks.publishToMavenLocal) }
}
