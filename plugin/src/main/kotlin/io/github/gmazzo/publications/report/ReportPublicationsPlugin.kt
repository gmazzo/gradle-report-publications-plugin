package io.github.gmazzo.publications.report

import io.github.gmazzo.publications.report.ReportPublicationSerializer.deserialize
import io.github.gmazzo.publications.report.ReportPublicationSerializer.serialize
import io.github.gmazzo.publications.report.spi.PublicationsCollector
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.flow.FlowScope
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.configuration.ShowStacktrace
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.kotlin.dsl.always
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.util.GradleVersion
import java.util.ServiceLoader
import javax.inject.Inject

class ReportPublicationsPlugin @Inject constructor(
    private val buildEventsListenerRegistry: BuildEventsListenerRegistry,
    private val flowScope: FlowScope,
) : Plugin<Project> {

    companion object {
        const val MIN_GRADLE_VERSION = "8.1"
    }

    override fun apply(project: Project): Unit = with(project) {
        check(GradleVersion.current() >= GradleVersion.version(MIN_GRADLE_VERSION)) {
            "Gradle version must be at least $MIN_GRADLE_VERSION"
        }

        if (project != rootProject) {
            rootProject.apply<ReportPublicationsPlugin>()
            return
        }

        val publications = createPublicationsCollector()
        val service = createCollectTaskOutcomeService()

        collectPublishTasksPublications(publications)

        if (gradle.parent == null) { // we only report at the root main build
            registerPublicationsReporter(publications, service)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Project.createPublicationsCollector() = with(gradle.rootBuild().extensions) {
        findByName("publicationsReport") as MapProperty<String, ByteArray>?
            ?: rootProject.objects.mapProperty<String, ByteArray>().also { add("publicationsReport", it) }
    }

    private fun Project.createCollectTaskOutcomeService() = gradle.sharedServices
        .registerIfAbsent("publicationsReport", ReportPublicationsService::class) {}
        .also(buildEventsListenerRegistry::onTaskCompletion)

    private fun Project.collectPublishTasksPublications(publications: MapProperty<String, ByteArray>) {
        val collectors = ServiceLoader.load(PublicationsCollector::class.java).toList().asSequence()
        val buildPath = gradle.path

        gradle.taskGraph.whenReady {
            publications.putAll(provider {
                allTasks.asSequence().flatMap { task ->
                    collectors
                        .flatMap { it.collectPublications(task) }
                        .mapNotNull { pub ->
                            val dryRunAwarePub =
                                if (gradle.startParameter.isDryRun && pub.outcome == ReportPublication.Outcome.Unknown)
                                    pub.copy(outcome = ReportPublication.Outcome.Skipped) else pub

                            try {
                                return@mapNotNull buildPath + task.path to serialize(dryRunAwarePub)

                            } catch (ex: Exception) {
                                logger.warn(
                                    "Failed to resolve publication for task ${task.path}",
                                    ex.takeIf { gradle.startParameter.showStacktrace == ShowStacktrace.ALWAYS })

                                return@mapNotNull null
                            }
                        }
                }.toMap()
            })
        }
    }

    private fun registerPublicationsReporter(
        publications: MapProperty<String, ByteArray>,
        service: Provider<ReportPublicationsService>,
    ) {
        flowScope.always(ReportPublicationsFlowAction::class) {
            parameters.publications.set(publications.map { it.mapValues { (_, pub) -> deserialize(pub) } })
            parameters.outcomes.set(service.map { it.tasksOutcome })
        }
    }

    private tailrec fun Gradle.rootBuild(): Gradle = when (val parent = parent) {
        null -> this
        else -> parent.rootBuild()
    }

    @Suppress("RecursivePropertyAccessor")
    private val Gradle.path: String
        get() = when (val parent = parent) {
            null -> ""
            else -> "${parent.path}${(this as GradleInternal).identityPath}"
        }

}
