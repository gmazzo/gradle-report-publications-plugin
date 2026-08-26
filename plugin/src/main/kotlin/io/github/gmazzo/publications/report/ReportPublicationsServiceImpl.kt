package io.github.gmazzo.publications.report

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.task.*

internal abstract class ReportPublicationsServiceImpl @Inject constructor(
    objects: ObjectFactory,
) : ReportPublicationsService {

    private val registered = AtomicBoolean()

    override val isFullyConfigured = true

    override val publications = objects
        .mapProperty<String, List<ReportPublication>>()
        .apply { finalizeValueOnRead() }

    override val outcomes: MutableMap<String, ReportPublication.Outcome> = ConcurrentHashMap()

    override fun noteRegistered() =
        registered.compareAndSet(false, true)

    override fun onFinish(event: FinishEvent) {
        when (event) {
            is TaskFinishEvent -> {
                val outcome = resolve(event.result) ?: return

                outcomes[event.descriptor.taskPath] = outcome
            }
        }
    }

    private fun resolve(result: TaskOperationResult) = when (result) {
        is TaskSuccessResult -> ReportPublication.Outcome.Published
        is TaskFailureResult -> ReportPublication.Outcome.Failed
        is TaskSkippedResult -> ReportPublication.Outcome.Skipped
        else -> null
    }

}
