/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.github.gmazzo.gradle.publications.report

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportPublicationsPluginTest {

    private val rootDir = File(System.getProperty("projectRootDir"))

    @Test
    fun `when run 'publish' on demo project, produces the expected output`() {
        val result = buildTest("publish")

        assertEquals(TaskOutcome.SUCCESS, result.task(":publish")?.outcome)
        assertEquals(null, result.task(":publishToMavenLocal")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":reportPublications")?.outcome)

        assertEquals(
            """
            > Task :reportPublications
            The following artifacts were published to myRepo(file:$rootDir/publish/build-logic/build/repo/):
             - io.gmazzo.demo.build-logic:build-logic:0.1.0
             - io.gmazzo.demo.build-logic:otherModule:0.1.0
            The following artifacts were published to myRepo(file:$rootDir/publish/build/repo/):
             - io.gmazzo.demo:demo:0.1.0
             - io.gmazzo.demo:module1:0.1.0
             - io.gmazzo.demo:module2:0.1.0
        """.trimIndent(), result.reportPublicationsOutput
        )
    }

    @Test
    fun `when run 'publishToMavenLocal' on demo project, produces the expected output`() {
        val result = buildTest("publishToMavenLocal")

        assertEquals(null, result.task(":publish")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":publishToMavenLocal")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":reportPublications")?.outcome)

        assertEquals(
            """
            > Task :reportPublications
            The following artifacts were published to mavenLocal(~/.m2/repository):
             - io.gmazzo.demo:demo:0.1.0
             - io.gmazzo.demo:module1:0.1.0
             - io.gmazzo.demo:module2:0.1.0
             - io.gmazzo.demo.build-logic:build-logic:0.1.0
             - io.gmazzo.demo.build-logic:otherModule:0.1.0
        """.trimIndent(), result.reportPublicationsOutput
        )
    }

    @Test
    fun `when run 'publish' and 'publishToMavenLocal' on demo project, produces the expected output`() {
        val result = buildTest("publish", "publishToMavenLocal")

        assertEquals(TaskOutcome.SUCCESS, result.task(":publish")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":publishToMavenLocal")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":reportPublications")?.outcome)

        assertEquals(
            """
            > Task :reportPublications
            The following artifacts were published to myRepo(file:$rootDir/publish-publishToMavenLocal/build-logic/build/repo/):
             - io.gmazzo.demo.build-logic:build-logic:0.1.0
             - io.gmazzo.demo.build-logic:otherModule:0.1.0
            The following artifacts were published to myRepo(file:$rootDir/publish-publishToMavenLocal/build/repo/):
             - io.gmazzo.demo:demo:0.1.0
             - io.gmazzo.demo:module1:0.1.0
             - io.gmazzo.demo:module2:0.1.0
            The following artifacts were published to mavenLocal(~/.m2/repository):
             - io.gmazzo.demo:demo:0.1.0
             - io.gmazzo.demo:module1:0.1.0
             - io.gmazzo.demo:module2:0.1.0
             - io.gmazzo.demo.build-logic:build-logic:0.1.0
             - io.gmazzo.demo.build-logic:otherModule:0.1.0
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
            .withProjectDir(rootDir)
            .withPluginClasspath()
            .withJaCoCo()
            .withArguments(*tasks)
            .forwardOutput()
            .build()
    }

    private val BuildResult.reportPublicationsOutput
        get() = "(> Task :reportPublications.*?)\\s*(?=> Task :)"
            .toRegex(RegexOption.DOT_MATCHES_ALL)
            .find(output)?.groupValues?.get(1)

    private fun GradleRunner.withJaCoCo() = apply {
        File(projectDir, "gradle.properties")
            .outputStream()
            .use(javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")::copyTo)
    }

}
