package io.github.gmazzo.gradle.publications.report

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

        publications.get().forEach { (repository, publications) ->
            text("The following artifacts are available at ")
            header.text(repository.name)
            if (repository.value.isNotBlank()) {
                text(" (")
                description.text(repository.value)
                text(")")
            }
            println(":")

            publications.forEach {
                text(" - ")
                identifier.text(it.group)
                text(":${it.artifact}:")
                info.text(it.version)
                if (it.outcome != ReportPublication.Outcome.Published) {
                    failure.text(" (${it.outcome})")
                }
                println()
            }
        }
    }

}
