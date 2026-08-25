package io.github.gmazzo.publications.report

import io.github.gmazzo.publications.report.ReportPublicationsService.Companion.reportsService
import io.github.gmazzo.publications.report.spi.PublicationsCollector
import java.util.*
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.configuration.BuildFeatures
import org.gradle.api.flow.FlowScope
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.TaskInternal
import org.gradle.api.invocation.Gradle
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.kotlin.dsl.always
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.util.GradleVersion

public class ReportPublicationsPlugin @Inject internal constructor(
    private val gradle: Gradle,
    private val buildEventsListenerRegistry: BuildEventsListenerRegistry,
    private val flowScope: FlowScope,
) : Plugin<Any> {

    public companion object {
        public const val MIN_GRADLE_VERSION: String = "8.8"
    }

    private val service = gradle.reportsService

    private val collectors = ServiceLoader
        .load(PublicationsCollector::class.java, PublicationsCollector::class.java.classLoader)
        .toList()

    override fun apply(target: Any) {
        check(GradleVersion.current() >= GradleVersion.version(MIN_GRADLE_VERSION)) {
            "Gradle version must be at least $MIN_GRADLE_VERSION"
        }

        when (target) {
            is Project -> {
                target.discoverTasks()

                if (!target.serviceOf<BuildFeatures>().isolatedProjects.active.get()) {
                    target.subprojects project@{ apply<ReportPublicationsPlugin>() }
                }
            }

            is Settings, is Gradle -> gradle.lifecycle.afterProject project@{
                apply<ReportPublicationsPlugin>()
            }

            else -> throw IllegalArgumentException("Unsupported target object: $target")
        }

        if (gradle.parent == null) { // we only report at the root main build
            registerPublicationsReporter()
        }
    }

    private fun Project.discoverTasks() {
        val publications = objects.mapProperty<String, List<ReportPublication>>()
            .also(service.get().publications::putAll)
            .apply { finalizeValueOnRead() }

        tasks.configureEach task@{
            if (collectors.any { it.accepts(this@task) }) {
                publications.put(this@task.identityPath, provider {
                    collectors.flatMap { it.collectPublications(this@task) }
                })
            }
        }
    }

    private fun registerPublicationsReporter() {
        if (service.get().noteRegistered()) {
            buildEventsListenerRegistry.onTaskCompletion(service)
            flowScope.always(ReportPublicationsFlowAction::class) {
                parameters {
                    publications.set(this@ReportPublicationsPlugin.service.flatMap { it.publications })
                    service.set(this@ReportPublicationsPlugin.service)
                }
            }
        }
    }

    private val Task.identityPath: String
        get() = (this as TaskInternal).identityPath.toString()

}
