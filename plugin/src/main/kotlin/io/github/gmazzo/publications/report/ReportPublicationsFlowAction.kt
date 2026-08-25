package io.github.gmazzo.publications.report

import java.io.*
import java.util.*
import javax.inject.Inject
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowParameters
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory

internal abstract class ReportPublicationsFlowAction : FlowAction<ReportPublicationsFlowAction.Params> {

    private val publicationsComparator =
        compareBy(ReportPublication::groupId, ReportPublication::artifactId, ReportPublication::version)

    override fun execute(parameters: Params) {
        val publications = parameters.publications.get() as Map<String, List<Serializable>>
        val outcomes = parameters.service.get().outcomes as Map<String, Enum<*>>

        val publicationsByRepo =
            TreeMap<ReportPublication.Repository, TreeSet<ReportPublication>>(compareBy(ReportPublication.Repository::value))

        for ((taskPath, pubs) in publications) {
            val outcome = recreate(outcomes[taskPath]) ?: continue

            for (pub in pubs) {
                val pub = recreate(pub)

                publicationsByRepo
                    .getOrPut(pub.repository) { TreeSet(publicationsComparator) }
                    .add(pub.copy(outcome = outcome))
            }
        }

        parameters.styledTextOutputFactory
            .create(ReportPublication::class.java)
            .report(publicationsByRepo)
    }

    private fun recreate(outcome: Enum<*>? /*ReportPublication.Outcome*/) = when (outcome) {
        null -> null
        is ReportPublication.Outcome -> outcome
        else -> ReportPublication.Outcome.valueOf(outcome.name)
    }

    private fun recreate(publication: Serializable /*ReportPublication*/) = when (publication) {
        is ReportPublication -> publication
        else -> ByteArrayOutputStream().use { out ->
            ObjectOutputStream(out).use { it.writeObject(publication) }
            ObjectInputStream(ByteArrayInputStream(out.toByteArray())).use { it.readObject() as ReportPublication }
        }
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
                it.outcome.displayName?.let { name -> failureHeader.text(" ($name)") }
                println()
            }
        }
    }

    private val ReportPublication.Outcome?.displayName get() = when(this) {
        ReportPublication.Outcome.Published -> null
        ReportPublication.Outcome.Failed -> "failed"
        ReportPublication.Outcome.Skipped -> "skipped"
        null-> "not run"
    }

    interface Params : FlowParameters {

        @get:Inject
        val styledTextOutputFactory: StyledTextOutputFactory

        @get:Input // this is intentionally redundant, to be able to recover them from Configuration Cache
        val publications: MapProperty<String, List<ReportPublication>>

        @get:ServiceReference
        val service: Property<ReportPublicationsService>

    }

}
