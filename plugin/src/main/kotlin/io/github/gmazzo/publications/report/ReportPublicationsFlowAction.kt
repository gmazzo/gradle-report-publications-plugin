package io.github.gmazzo.publications.report

import java.util.*
import javax.inject.Inject
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory

internal abstract class ReportPublicationsFlowAction : FlowAction<ReportPublicationsFlowAction.Params> {

    private val publicationsComparator =
        compareBy(ReportPublication::groupId, ReportPublication::artifactId, ReportPublication::version)

    override fun execute(parameters: Params) {
        val service = parameters.service.get()
        val logger = parameters.styledTextOutputFactory.create(ReportPublication::class.java)

        val publications =
            TreeMap<ReportPublication.Repository, TreeSet<ReportPublication>>(compareBy(ReportPublication.Repository::value))

        for (pub in service.collectPublications()) {
            publications.compute(pub.repository) { _, set ->
                (set ?: TreeSet(publicationsComparator)).apply { add(pub) }
            }
        }

        logger.report(publications)
    }

    private fun StyledTextOutput.report(publications: TreeMap<ReportPublication.Repository, TreeSet<ReportPublication>>) {
        val header = withStyle(StyledTextOutput.Style.Header)
        val description = withStyle(StyledTextOutput.Style.Description)
        val identifier = withStyle(StyledTextOutput.Style.Identifier)
        val info = withStyle(StyledTextOutput.Style.Info)
        val failure = withStyle(StyledTextOutput.Style.Failure)
        val failureHeader = withStyle(StyledTextOutput.Style.FailureHeader)

        println()
        publications.forEach { (repository, publications) ->
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
                failure.text(it.artifacts.joinToString(prefix = " [", separator = ", ", postfix = "]"))
                if (it.outcome != ReportPublication.Outcome.Published) {
                    failureHeader.text(" (${it.outcome.name.lowercase()})")
                }
                println()
            }
        }
    }

    interface Params : FlowParameters {

        @get:Inject
        val styledTextOutputFactory: StyledTextOutputFactory

        @get:ServiceReference
        val service: Property<ReportPublicationsService>

    }

}
