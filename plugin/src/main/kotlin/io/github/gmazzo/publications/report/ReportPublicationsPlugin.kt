package io.github.gmazzo.publications.report

import io.github.gmazzo.publications.report.ReportPublicationsService.Companion.reportsService
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.configuration.BuildFeatures
import org.gradle.api.flow.FlowScope
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.build.event.BuildEventsListenerRegistry
import org.gradle.kotlin.dsl.always
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

    override fun apply(target: Any) {
        check(GradleVersion.current() >= GradleVersion.version(MIN_GRADLE_VERSION)) {
            "Gradle version must be at least $MIN_GRADLE_VERSION"
        }

        discoverTasks(target)

        if (gradle.parent == null) { // we only report at the root main build
            registerPublicationsReporter()
        }
    }

    private fun discoverTasks(target: Any) {
        val buildPath = gradle.path

        when (target) {
            is Project -> {
                service.get().onConfigure(buildPath, target)

                if (!target.serviceOf<BuildFeatures>().isolatedProjects.active.get()) {
                    target.subprojects project@{ service.get().onConfigure(buildPath, this@project) }
                }
            }

            is Settings, is Gradle -> gradle.lifecycle.afterProject project@{
                this@project.gradle.reportsService.get().onConfigure(buildPath, this@project)
            }

            else -> throw IllegalArgumentException("Unsupported target object: $target")
        }

        gradle.taskGraph.whenReady {
            service.get().onTaskGraph(allTasks)
        }
    }

    private fun registerPublicationsReporter() {
        if (service.get().noteRegistered()) {
            buildEventsListenerRegistry.onTaskCompletion(service)
            flowScope.always(ReportPublicationsFlowAction::class) {
                parameters.service.set(service)
            }
        }
    }

    private val Gradle.path: String
        get() = when (val parent = parent) {
            null -> ""
            else -> "${parent.path}${(this as GradleInternal).identityPath}"
        }

}
