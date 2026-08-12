import org.gradle.api.internal.GradleInternal

plugins {
    java
    `ivy-publish`
    `maven-publish`
}

group = "io.gmazzo.demo" + (if (gradle.parent != null) ".build-logic" else "")
version = "0.1.0"
java.toolchain.languageVersion = JavaLanguageVersion
    .of(the<VersionCatalogsExtension>().named("libs").findVersion("java").get().requiredVersion)

val rootDir = (gradle as GradleInternal).root.settings.layout.rootDirectory

plugins.withId("signing") {
    configure<SigningExtension> {
        useInMemoryPgpKeys(providers.fileContents(rootDir.file("demo/singingkey.asc")).asText.get(), "org.demo")
        sign(publishing.publications)
    }
}

publishing {
    publications {
        create<MavenPublication>("javaMaven") { from(components["java"]) }
        create<IvyPublication>("javaIvy") { from(components["java"]) }
    }
    repositories {
        val repos = rootDir.dir("build/repo")

        maven(url = repos.dir("maven")) { name = "localMaven" }
        ivy(url = repos.dir("ivy")) { name = "localIvy" }
    }
}

// CC issue with Ivy publication tasks
tasks.withType<PublishToIvyRepository>().configureEach { mustRunAfter(tasks.withType<Sign>()) }
tasks.withType<AbstractPublishToMaven>().configureEach { mustRunAfter(tasks.withType<Sign>()) }

