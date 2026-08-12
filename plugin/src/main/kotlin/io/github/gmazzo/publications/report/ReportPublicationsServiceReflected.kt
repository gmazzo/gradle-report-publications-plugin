package io.github.gmazzo.publications.report

import io.github.gmazzo.publications.report.ReportPublicationsServiceImpl.Companion.wrap
import java.io.Serializable
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.tooling.events.FinishEvent

internal abstract class ReportPublicationsServiceReflected : ReportPublicationsService {

    private val delegate: Any = parameters.delegate.get()

    private val noteRegisteredImpl = delegate
        .javaClass.getMethod("noteRegistered")

    private val onConfigureImpl = delegate
        .javaClass.getMethod("onConfigure", String::class.java, Project::class.java)

    private val onTaskGraphImpl = delegate
        .javaClass.getMethod("onTaskGraph", List::class.java)

    private val onFinishImpl = delegate
        .javaClass.getMethod("onFinish", FinishEvent::class.java)

    private val collectPublicationsImpl = delegate
        .javaClass.getMethod("collectPublications")

    override fun noteRegistered() =
        noteRegisteredImpl.invoke(delegate) as Boolean

    override fun onConfigure(buildPath: String, project: Project) {
        onConfigureImpl(delegate, buildPath, project)
    }

    override fun onTaskGraph(allTasks: List<Task>) {
        onTaskGraphImpl(delegate, allTasks)
    }

    override fun onFinish(event: FinishEvent) {
        onFinishImpl(delegate, event)
    }

    @Suppress("UNCHECKED_CAST")
    override fun collectPublications() =
        (collectPublicationsImpl(delegate) as List<Serializable>).map(::wrap)

}
