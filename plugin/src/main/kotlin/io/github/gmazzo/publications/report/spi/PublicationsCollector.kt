package io.github.gmazzo.publications.report.spi

import io.github.gmazzo.publications.report.ReportPublication
import org.gradle.api.Task

fun interface PublicationsCollector {

    fun collectPublications(task: Task): List<ReportPublication>

}
