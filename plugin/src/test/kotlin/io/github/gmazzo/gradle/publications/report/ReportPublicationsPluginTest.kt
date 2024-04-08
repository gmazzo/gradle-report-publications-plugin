package io.github.gmazzo.gradle.publications.report

import io.github.gmazzo.publications.report.ReportPublicationsPlugin
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

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
            The following artifacts were published to myRepo(file:$rootDir/publish/build-logic/build/repo/):
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar]
            The following artifacts were published to myRepo(file:$rootDir/publish/build/repo/):
             - io.gmazzo.demo:demo:0.1.0 [jar]
             - io.gmazzo.demo:module1:0.1.0 [jar] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar]
        """.trimIndent(), result.reportPublicationsOutput
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
             - io.gmazzo.demo:demo:0.1.0 [jar]
             - io.gmazzo.demo:module1:0.1.0 [jar] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar]
        """.trimIndent(), result.reportPublicationsOutput
        )
    }

    @Test
    fun `when run 'publish' and 'publishToMavenLocal' on demo project, produces the expected output`() {
        val result = buildTest("publish", "publishToMavenLocal")

        assertEquals(TaskOutcome.SUCCESS, result.task(":publish")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":publishToMavenLocal")?.outcome)

        assertEquals(
            """
            The following artifacts were published to myRepo(file:$rootDir/publish-publishToMavenLocal/build-logic/build/repo/):
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar]
            The following artifacts were published to myRepo(file:$rootDir/publish-publishToMavenLocal/build/repo/):
             - io.gmazzo.demo:demo:0.1.0 [jar]
             - io.gmazzo.demo:module1:0.1.0 [jar] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar]
            The following artifacts were published to mavenLocal(~/.m2/repository):
             - io.gmazzo.demo:demo:0.1.0 [jar]
             - io.gmazzo.demo:module1:0.1.0 [jar] (skipped)
             - io.gmazzo.demo:module2:0.1.0 [jar]
             - io.gmazzo.demo.build-logic:build-logic:0.1.0 [jar]
             - io.gmazzo.demo.build-logic:otherModule:0.1.0 [jar]
        """.trimIndent(), result.reportPublicationsOutput
        )
    }

    private fun buildTest(vararg tasks: String): BuildResult {
        val rootDir = File(rootDir, tasks.joinToString(separator = "-")).apply {
            deleteRecursively()
            mkdirs()
        }

        File("../demo").copyRecursively(rootDir)
        File(rootDir, "settings.gradle.kts").writeText(
            """
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
            .withJaCoCo()
            .withArguments(*tasks)
            .forwardOutput()
            .build()
    }

    private val BuildResult.reportPublicationsOutput
        get() = "(The following artifacts were published.*?)\\s*(?=BUILD SUCCESSFUL)"
            .toRegex(RegexOption.DOT_MATCHES_ALL)
            .find(output)?.groupValues?.get(1)

    private fun GradleRunner.withJaCoCo() = apply {
        File(projectDir, "gradle.properties")
            .outputStream()
            .use(javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")::copyTo)
    }

}
