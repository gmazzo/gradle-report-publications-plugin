package io.github.gmazzo.publications.report

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFailureResult
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskOperationResult
import org.gradle.tooling.events.task.TaskSkippedResult
import org.gradle.tooling.events.task.TaskSuccessResult

abstract class ReportPublicationsService :
    BuildService<BuildServiceParameters.None>,
    OperationCompletionListener {

    val tasksOutcome = mutableMapOf<String, ReportPublication.Outcome>()

    override fun onFinish(event: FinishEvent) {
        when (event) {
            is TaskFinishEvent -> tasksOutcome[event.descriptor.taskPath] = resolve(event.result)
        }
    }

    private fun resolve(result: TaskOperationResult) = when(result) {
        is TaskSuccessResult -> ReportPublication.Outcome.Published
        is TaskFailureResult -> ReportPublication.Outcome.Failed
        is TaskSkippedResult -> ReportPublication.Outcome.Skipped
        else -> ReportPublication.Outcome.Unknown
    }

}
