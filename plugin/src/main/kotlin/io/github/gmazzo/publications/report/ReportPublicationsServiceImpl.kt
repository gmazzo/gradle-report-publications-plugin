package io.github.gmazzo.publications.report

import io.github.gmazzo.publications.report.spi.PublicationsCollector
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logging
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.task.TaskFailureResult
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskOperationResult
import org.gradle.tooling.events.task.TaskSkippedResult
import org.gradle.tooling.events.task.TaskSuccessResult

internal abstract class ReportPublicationsServiceImpl : ReportPublicationsService {

    private val registered = AtomicBoolean()

    private val candidates: MutableMap<Task, String> = ConcurrentHashMap()

    private val tasksToRun: MutableSet<Task> = ConcurrentHashMap.newKeySet()

    private val outcomes: MutableMap<String, ReportPublication.Outcome> = ConcurrentHashMap()

    private val logger =
        Logging.getLogger(ReportPublicationsServiceImpl::class.java)

    private val collectors = ServiceLoader
        .load(PublicationsCollector::class.java, PublicationsCollector::class.java.classLoader)
        .toList()

    override fun noteRegistered() =
        registered.compareAndSet(false, true)

    override fun onConfigure(buildPath: String, project: Project) {
        for (collector in collectors) {
            project.tasks.withType(collector.accepts).configureEach task@{
                candidates[this@task] = buildPath + path
            }
        }
    }

    override fun onTaskGraph(allTasks: List<Task>) {
        tasksToRun.addAll(allTasks)
    }

    override fun onFinish(event: FinishEvent) {
        when (event) {

            is TaskFinishEvent -> outcomes[event.descriptor.taskPath] = resolve(event.result)
        }
    }

    override fun collectPublications() = candidates.asSequence()
        .filter { (task, _) -> task in tasksToRun }
        .flatMap { (task, taskPath) ->
            guard(task) {
                collectors.asSequence().flatMap { collector ->
                    collector
                        .collectPublications(task)
                        .map { computeOutcome(taskPath, it) }
                }
            } ?: emptySequence()
        }
        .toList()

    private fun computeOutcome(taskPath: String, publication: Serializable /*ReportPublication*/): ReportPublication {
        val outcome =
            outcomes[taskPath] ?: if (parameters.dryRun.get()) ReportPublication.Outcome.Skipped
            else ReportPublication.Outcome.Unknown

        return wrap(publication).copy(outcome = outcome)
    }

    private fun resolve(result: TaskOperationResult) = when (result) {
        is TaskSuccessResult -> ReportPublication.Outcome.Published
        is TaskFailureResult -> ReportPublication.Outcome.Failed
        is TaskSkippedResult -> ReportPublication.Outcome.Skipped
        else -> ReportPublication.Outcome.Unknown
    }

    private fun <Return> guard(task: Task, block: () -> Return): Return? = try {
        block()

    } catch (ex: Exception) {
        logger.warn(
            "Failed to resolve publication for task ${task.path}",
            ex.takeIf { parameters.verbose.get() })
        null
    }

    @Suppress("UNCHECKED_CAST")
    private fun <Type : Task> PublicationsCollector<Type>.collectPublications(task: Task) =
        if (accepts.isInstance(task)) collectPublications(task as Type)
        else emptyList()

    companion object {

        @Suppress("USELESS_IS_CHECK", "REDUNDANT_ELSE_IN_WHEN")
        fun wrap(publication: Serializable /*ReportPublication*/) = when (publication) {
            is ReportPublication -> publication
            else -> ByteArrayOutputStream().use { out ->
                ObjectOutputStream(out).use { it.writeObject(publication) }
                ObjectInputStream(ByteArrayInputStream(out.toByteArray())).use { it.readObject() as ReportPublication }
            }
        }

    }

}
