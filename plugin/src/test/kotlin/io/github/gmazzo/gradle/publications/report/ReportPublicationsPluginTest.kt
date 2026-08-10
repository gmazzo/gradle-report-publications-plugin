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

sealed class ReportPublicationsPluginTest(
    private val gradleVersion: String,
    private val isolatedProjects: Boolean = false,
) {

    class Min : ReportPublicationsPluginTest(ReportPublicationsPlugin.MIN_GRADLE_VERSION)
    class Current : ReportPublicationsPluginTest(GradleVersion.current().baseVersion.version)
    class CurrentIsolated : ReportPublicationsPluginTest(GradleVersion.current().baseVersion.version, isolatedProjects = true)

    private val rootDir = File(System.getProperty("projectRootDir"), gradleVersion)
        .resolve(if (isolatedProjects) "isolated" else "default")

    @Test
    fun `when run 'publish' on demo project, produces the expected output`() {
        val result = buildTest("publish")

        assertEquals(TaskOutcome.SUCCESS, result.task(":demo:publish")?.outcome)
        assertEquals(null, result.task(":demo:publishToMavenLocal")?.outcome)

        assertEquals(
            """
            The following artifacts were published to localIvy(${rootDir.resolve("publish/build/repo/ivy/").toURI()}):
             - io.gmazzo.demo:demo:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module2:0.1.0 [jar, xml, module]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, xml, module]
            The following artifacts were published to localMaven(${
                rootDir.resolve("publish/build/repo/maven/").toURI()
            }):
             - io.gmazzo.demo:demo:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar, pom, module]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, pom, module]
            """.trimIndent(), result.reportPublicationsOutput?.normaliseLineSeparators()
        )
    }

    @Test
    fun `when run 'publishToMavenLocal' on demo project, produces the expected output`() {
        val result = buildTest("publishToMavenLocal")

        assertEquals(null, result.task(":demo:publish")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":demo:publishToMavenLocal")?.outcome)

        assertEquals(
            """
            The following artifacts were published to mavenLocal(~/.m2/repository):
             - io.gmazzo.demo:demo:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar, pom, module]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, pom, module]
            """.trimIndent(), result.reportPublicationsOutput?.normaliseLineSeparators()
        )
    }

    @Test
    fun `when run 'publish' and 'publishToMavenLocal' on demo project, produces the expected output`() {
        val result = buildTest("publish", "publishToMavenLocal")

        assertEquals(TaskOutcome.SUCCESS, result.task(":demo:publish")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":demo:publishToMavenLocal")?.outcome)

        assertEquals(
            """
            The following artifacts were published to localIvy(${
                rootDir.resolve("publish-publishToMavenLocal/build/repo/ivy/").toURI()
            }):
             - io.gmazzo.demo:demo:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, xml, module, xml.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module2:0.1.0 [jar, xml, module]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, xml, module]
            The following artifacts were published to localMaven(${
                rootDir.resolve("publish-publishToMavenLocal/build/repo/maven/").toURI()
            }):
             - io.gmazzo.demo:demo:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar, pom, module]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, pom, module]
            The following artifacts were published to mavenLocal(~/.m2/repository):
             - io.gmazzo.demo:demo:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc]
             - io.gmazzo.demo:module1:0.1.0 [jar, pom, module, pom.asc, module.asc, jar.asc] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar, pom, module]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar, pom, module]
            """.trimIndent(), result.reportPublicationsOutput?.normaliseLineSeparators()
        )
    }

    private fun buildTest(vararg tasks: String): BuildResult {
        val rootDir = rootDir.resolve(tasks.joinToString(separator = "-")).apply {
            deleteRecursively()
            mkdirs()
        }

        File("../demo").copyRecursively(rootDir.resolve("demo"))
        File("../gradle/libs.versions.toml").apply {
            copyTo(File(rootDir, "gradle/libs.versions.toml"))
            copyTo(File(rootDir, "build-logic/gradle/libs.versions.toml"))
        }
        rootDir.resolve("settings.gradle.kts").apply {
            File("../settings.gradle.kts").copyTo(this)
            appendText("\nrootProject.name = \"demo-${tasks.joinToString(separator = "-")}\"")
        }

        rootDir.walkTopDown().filter { it.name == "settings.gradle.kts" }.forEach { file ->
            file.writeText(file.readText().replace("includeBuild\\(\".*?plugin\"\\)".toRegex(), ""))
        }

        return GradleRunner.create()
            .withGradleVersion(gradleVersion)
            .withProjectDir(rootDir)
            .withPluginClasspath()
            .withArguments(listOfNotNull(
                "--stacktrace",
                "--warning-mode",
                "all",
                "--isolated-projects".takeIf { isolatedProjects },
            ) + tasks)
            .forwardOutput()
            .build()
    }

    private val BuildResult.reportPublicationsOutput
        get() = "(The following artifacts were published.*?)\\s*(?=BUILD SUCCESSFUL)"
            .toRegex(RegexOption.DOT_MATCHES_ALL)
            .find(output)?.groupValues?.get(1)

}
