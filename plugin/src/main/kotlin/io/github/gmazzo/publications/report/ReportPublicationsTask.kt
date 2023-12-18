package io.github.gmazzo.publications.report

import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory
import javax.inject.Inject

@UntrackedTask(because = "not meant to be cached")
internal abstract class ReportPublicationsTask @Inject constructor(
    styledTextOutputFactory: StyledTextOutputFactory,
) : DefaultTask() {

    @get:Input
    abstract val publications: MapProperty<ReportPublication.Repository, Set<ReportPublication>>

    @field:Transient
    private val styledLogger = styledTextOutputFactory.create(javaClass)

    @TaskAction
    fun reportPublications(): Unit = with(styledLogger) {
        val header = withStyle(StyledTextOutput.Style.Header)
        val description = withStyle(StyledTextOutput.Style.Description)
        val identifier = withStyle(StyledTextOutput.Style.Identifier)
        val info = withStyle(StyledTextOutput.Style.Info)
        val failure = withStyle(StyledTextOutput.Style.Failure)
        val failureHeader = withStyle(StyledTextOutput.Style.FailureHeader)

        publications.get().forEach { (repository, publications) ->
            text("The following artifacts were published to ")
            header.text(repository.name)
            if (repository.value.isNotBlank()) {
                text("(")
                description.text(repository.value)
                text(")")
            }
            println(":")

            publications.forEach {
                text(" - ")
                identifier.text(it.groupId)
                text(":${it.artifactId}:")
                info.text(it.version)
                failure.text(it.artifacts.joinToString(prefix = " ", separator = " "))
                if (it.outcome != ReportPublication.Outcome.Published) {
                    failureHeader.text(" (${it.outcome.text})")
                }
                println()
            }
        }
    }

}
