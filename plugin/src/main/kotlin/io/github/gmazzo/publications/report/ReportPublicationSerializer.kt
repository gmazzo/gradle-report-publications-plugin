package io.github.gmazzo.publications.report

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Short story about this class:
 *
 * The plugin supports included builds, but it requires to also get applied to them.
 * As it's applied multiple times on different classloaders (one per root project), the classes loaded
 * on each classloader are effectively different classes, and the reported (loaded at the main build) can't see them.
 *
 * As a workaround, we serialize the data when populating the [ReportPublication] collection, and
 * we deserialize them before sending to the [ReportPublicationsFlowAction] reporter.
 *
 * The "right" way to address this classloader issue, will be to convert [ReportPublicationsPlugin]
 * to a [org.gradle.api.initialization.Settings] plugin, as its classpath will be parent one of the included builds.
 * This option was discarded because the extra complexity that [org.gradle.api.initialization.Settings] plugins have:
 * 1) TOML is not supported for them
 * 2) You need to define the plugin version on every settings `plugins` closure
 */
public object ReportPublicationSerializer {

    public fun serialize(publication: ReportPublication): ByteArray = with(ByteArrayOutputStream()) {
        ObjectOutputStream(this).use { it.writeObject(publication) }
        toByteArray()
    }

    public fun deserialize(bytes: ByteArray): ReportPublication = bytes.inputStream().use {
        ObjectInputStream(it).readObject() as ReportPublication
    }

}
