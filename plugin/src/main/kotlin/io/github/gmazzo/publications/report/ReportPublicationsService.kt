package io.github.gmazzo.publications.report

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.GradleInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.configuration.ShowStacktrace
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.ServiceReference
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.tooling.events.OperationCompletionListener

internal interface ReportPublicationsService :
    BuildService<ReportPublicationsService.Params>,
    OperationCompletionListener {

    fun noteRegistered(): Boolean

    fun onConfigure(buildPath: String, project: Project)

    fun onTaskGraph(allTasks: List<Task>)

    fun collectPublications(): List<ReportPublication>

    interface Params : BuildServiceParameters {
        val verbose: Property<Boolean>
        val dryRun: Property<Boolean>
        @get:ServiceReference(SERVICE_NAME) val delegate: Property<Any>
    }

    companion object {
        const val SERVICE_NAME: String = "publicationsReport"

        internal val Gradle.reportsService: Provider<out ReportPublicationsService>
            get() = with((this as GradleInternal).root.sharedServices) {
                when(val existing = registrations.findByName(SERVICE_NAME)) {
                    null -> registerIfAbsent(SERVICE_NAME, ReportPublicationsServiceImpl::class) {
                        parameters {
                            verbose.set(gradle.startParameter.showStacktrace == ShowStacktrace.ALWAYS)
                            dryRun.set(gradle.startParameter.isDryRun)
                        }
                    }
                    else -> registerIfAbsent(
                        "${SERVICE_NAME}_${ReportPublicationsServiceReflected::class.java.hashCode()}",
                        ReportPublicationsServiceReflected::class
                    ) {
                        parameters {
                            @Suppress("UNCHECKED_CAST")
                            delegate.set(existing.service as Provider<Any>)
                        }
                    }
                }
            }

    }

}
