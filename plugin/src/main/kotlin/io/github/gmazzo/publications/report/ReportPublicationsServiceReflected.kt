package io.github.gmazzo.publications.report

import javax.inject.Inject
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.services.BuildService
import org.gradle.tooling.events.FinishEvent

internal abstract class ReportPublicationsServiceReflected @Inject constructor(
    objects: ObjectFactory,
) : ReportPublicationsServiceImpl(objects) {

    private val logger =
        Logging.getLogger(ReportPublicationsServiceReflected::class.java)

    private val delegate =
        parameters.delegate.get()

    private val publicationsImpl = delegate.resolve("getPublications")

    private val outcomesImpl = delegate.resolve("getOutcomes")

    private val noteRegisteredImpl = delegate.resolve("noteRegistered")

    private val onFinishImpl = delegate.resolve("onFinish", FinishEvent::class.java)

    override val isFullyConfigured =
        publicationsImpl != null &&
            outcomesImpl != null &&
            noteRegisteredImpl != null &&
            onFinishImpl != null

    @Suppress("UNCHECKED_CAST")
    override val publications =
        publicationsImpl?.invoke(delegate) as MapProperty<String, List<ReportPublication>>? ?: super.publications

    @Suppress("UNCHECKED_CAST")
    override val outcomes =
        outcomesImpl?.invoke(delegate) as MutableMap<String, ReportPublication.Outcome>? ?: super.outcomes

    override fun noteRegistered() =
        noteRegisteredImpl?.invoke(delegate) as Boolean? ?: super.noteRegistered()

    override fun onFinish(event: FinishEvent) {
        onFinishImpl?.invoke(delegate, event) ?: super.onFinish(event)
    }

    private fun BuildService<*>.resolve(method: String, vararg args: Class<*>?) = try {
        this@resolve.javaClass.getMethod(method, *args)

    } catch (e: NoSuchMethodException) {
        logger.warn(
            "Failed to resolve method $method for ${this@resolve.javaClass}. " +
                "This is usually caused by different plugins versions is the classpath",
            e.takeIf { this@ReportPublicationsServiceReflected.parameters.verbose.get() },
        )
        null
    }

}
