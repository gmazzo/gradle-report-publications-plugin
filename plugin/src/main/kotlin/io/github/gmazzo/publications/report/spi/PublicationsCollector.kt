package io.github.gmazzo.publications.report.spi

import io.github.gmazzo.publications.report.ReportPublication
import org.gradle.api.Task

public interface PublicationsCollector {

    public fun accepts(task: Task): Boolean

    public fun collectPublications(task: Task): List<ReportPublication>

}
