package io.github.gmazzo.gradle.publications.report

import io.github.gmazzo.publications.report.ReportPublicationsPlugin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import org.gradle.kotlin.dsl.support.normaliseLineSeparators
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion

sealed class ReportPublicationsPluginTest(private val gradleVersion: String) {

    class Min : ReportPublicationsPluginTest(ReportPublicationsPlugin.MIN_GRADLE_VERSION)
    class Current : ReportPublicationsPluginTest(GradleVersion.current().baseVersion.version)

    private val rootDir = File(System.getProperty("projectRootDir"), gradleVersion)

    @Test
    fun `when run 'publish' on demo project, produces the expected output`() {
        val result = buildTest("publish")

        assertEquals(TaskOutcome.SUCCESS, result.task(":publish")?.outcome)
        assertEquals(null, result.task(":publishToMavenLocal")?.outcome)

        assertEquals(
            """
            The following artifacts were published to localIvy(${rootDir.resolve("publish/build/repo/ivy/").toURI()}):
             - io.gmazzo.demo:demo:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module2:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, xml, module]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar, xml, module]
            The following artifacts were published to localMaven(${
                rootDir.resolve("publish/build/repo/maven/").toURI()
            }):
             - io.gmazzo.demo:demo:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, pom, module]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar, pom, module]
            """.trimIndent(), result.reportPublicationsOutput?.normaliseLineSeparators()
        )
    }

    @Test
    fun `when run 'publishToMavenLocal' on demo project, produces the expected output`() {
        val result = buildTest("publishToMavenLocal")

        assertEquals(null, result.task(":publish")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":publishToMavenLocal")?.outcome)

        assertEquals(
            """
            The following artifacts were published to mavenLocal(~/.m2/repository):
             - io.gmazzo.demo:demo:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, pom, module]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar, pom, module]
            """.trimIndent(), result.reportPublicationsOutput?.normaliseLineSeparators()
        )
    }

    @Test
    fun `when run 'publish' and 'publishToMavenLocal' on demo project, produces the expected output`() {
        val result = buildTest("publish", "publishToMavenLocal")

        assertEquals(TaskOutcome.SUCCESS, result.task(":publish")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":publishToMavenLocal")?.outcome)

        assertEquals(
            """
            The following artifacts were published to localIvy(${
                rootDir.resolve("publish-publishToMavenLocal/build/repo/ivy/").toURI()
            }):
             - io.gmazzo.demo:demo:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module2:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, xml, module]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar, xml, module]
            The following artifacts were published to localMaven(${
                rootDir.resolve("publish-publishToMavenLocal/build/repo/maven/").toURI()
            }):
             - io.gmazzo.demo:demo:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, pom, module]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar, pom, module]
            The following artifacts were published to mavenLocal(~/.m2/repository):
             - io.gmazzo.demo:demo:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, pom, module]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar, pom, module]
            """.trimIndent(), result.reportPublicationsOutput?.normaliseLineSeparators()
        )
    }

    private fun buildTest(vararg tasks: String): BuildResult {
        val rootDir = File(rootDir, tasks.joinToString(separator = "-")).apply {
            deleteRecursively()
            mkdirs()
        }

        File("../demo").copyRecursively(rootDir)
        File("../gradle/libs.versions.toml").apply {
            copyTo(File(rootDir, "gradle/libs.versions.toml"))
            copyTo(File(rootDir, "build-logic/gradle/libs.versions.toml"))
        }
        File(rootDir, "settings.gradle.kts").writeText(
            """
            plugins {
                id("jacoco-testkit-coverage")
            }

            rootProject.name = "demo"

            includeBuild("build-logic")
            include("module1")
            include("module2")
            """.trimIndent()
        )

        return GradleRunner.create()
            .withGradleVersion(gradleVersion)
            .withProjectDir(rootDir)
            .withPluginClasspath()
            .withArguments("--stacktrace", "--no-configuration-cache", "--warning-mode", "all", *tasks)
            .forwardOutput()
            .build()
    }

    private val BuildResult.reportPublicationsOutput
        get() = "(The following artifacts were published.*?)\\s*(?=BUILD SUCCESSFUL)"
            .toRegex(RegexOption.DOT_MATCHES_ALL)
            .find(output)?.groupValues?.get(1)

}
