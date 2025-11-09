package io.github.gmazzo.publications.report.spi

import io.github.gmazzo.publications.report.ReportPublication
import org.gradle.api.Task

public fun interface PublicationsCollector {

    public fun collectPublications(task: Task): List<ReportPublication>

}
