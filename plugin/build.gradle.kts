import java.lang.Thread.sleep

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.samReceiver)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.jacoco.testkit)
    signing
}

group = "io.github.gmazzo.publications.report"
description = "Gradle Publications Report Plugin"
version = providers
    .exec { commandLine("git", "describe", "--tags", "--always") }
    .standardOutput.asText.get().trim().removePrefix("v")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
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
            tags.addAll("maven", "publication", "maven-publish", "report")
        }
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    testImplementation(gradleKotlinDsl())

    compileOnly(libs.autoservice.annotations)
    ksp(libs.autoservice.ksp)
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    useInMemoryPgpKeys(signingKey, signingPassword)
    publishing.publications.configureEach(::sign)
    tasks.withType<Sign>().configureEach { enabled = signingKey != null }
}

tasks.publish {
    dependsOn(tasks.publishPlugins)
}

testing.suites.withType<JvmTestSuite> {
    useKotlinTest()
}

tasks.test {
    systemProperty("projectRootDir", temporaryDir)
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports.xml.required = true
}
