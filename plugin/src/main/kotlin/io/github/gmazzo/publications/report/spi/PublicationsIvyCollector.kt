package io.github.gmazzo.publications.report.spi

import com.google.auto.service.AutoService
import io.github.gmazzo.publications.report.ReportPublication
import org.gradle.api.Task
import org.gradle.api.publish.ivy.IvyArtifact
import org.gradle.api.publish.ivy.tasks.PublishToIvyRepository

@AutoService(PublicationsCollector::class)
internal class PublicationsIvyCollector : PublicationsCollector {

    override fun collectPublications(task: Task): List<ReportPublication> {
        if (task !is PublishToIvyRepository) return emptyList()

        val repository = ReportPublication.Repository(
            name = task.repository.name,
            value = task.repository.url.toString()
        )
        val artifacts = task.publication.artifacts.sortedWith(compareBy(IvyArtifact::getClassifier)).map {
            when (val classifier = it.classifier) {
                null -> it.extension
                else -> "$classifier.${it.extension}"
            }
        }.ifEmpty { listOf("ivy") }

        return listOf(
            ReportPublication(
                groupId = task.publication.organisation,
                artifactId = task.publication.module,
                version = task.publication.revision,
                repository = repository,
                outcome = ReportPublication.Outcome.Unknown,
                artifacts = artifacts
            )
        )
    }

}
