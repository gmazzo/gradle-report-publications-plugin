plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.gradle.pluginPublish)
}

group = "io.github.gmazzo.publications.report"
description = "Gradle Publications Report Plugin"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))
samWithReceiver.annotation(HasImplicitReceiver::class.qualifiedName!!)

gradlePlugin {
    website.set("https://github.com/gmazzo/gradle-report-publications-plugin")
    vcsUrl.set("https://github.com/gmazzo/gradle-report-publications-plugin")

    plugins {
        create("publications-report") {
            id = "io.github.gmazzo.publications.report"
            displayName = name
            implementationClass = "io.github.gmazzo.publications.report.ReportPublicationsPlugin"
            description = "Decorates the build logs with maven coordinates of artifacts published with `publish` or `publishToMavenLocal`"
            tags.addAll("maven", "gradle", "publication", "maven-publish", "report")
        }
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    testImplementation(gradleKotlinDsl())
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}

testing.suites.withType<JvmTestSuite> {
    useKotlinTest()
}

tasks.test {
    systemProperty("projectRootDir", temporaryDir)
}
