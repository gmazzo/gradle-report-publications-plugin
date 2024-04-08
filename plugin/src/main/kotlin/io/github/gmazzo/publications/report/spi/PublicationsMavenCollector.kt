package io.github.gmazzo.publications.report.spi

import com.google.auto.service.AutoService
import io.github.gmazzo.publications.report.ReportPublication
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

@AutoService(PublicationsCollector::class)
internal class PublicationsMavenCollector  : PublicationsCollector {

    override fun collectPublications(task: Task): List<ReportPublication> {
        if (task !is AbstractPublishToMaven) return emptyList()

        val repository = when (task) {
            is PublishToMavenLocal -> ReportPublication.Repository(
                name = "mavenLocal",
                value = "~/.m2/repository"
            )

            is PublishToMavenRepository -> ReportPublication.Repository(
                name = task.repository.name,
                value = task.repository.url.toString()
            )

            else -> ReportPublication.Repository(name = "<unknown>", value = "")
        }
        val artifacts = task.publication.artifacts.sortedWith(compareBy(MavenArtifact::getClassifier)).map {
            when (val classifier = it.classifier) {
                null -> it.extension
                else -> "$classifier.${it.extension}"
            }
        }.ifEmpty { listOf("pom") }

        return listOf(
            ReportPublication(
                groupId = task.publication.groupId,
                artifactId = task.publication.artifactId,
                version = task.publication.version,
                repository = repository,
                outcome = ReportPublication.Outcome.Unknown,
                artifacts = artifacts
            )
        )
    }

}
