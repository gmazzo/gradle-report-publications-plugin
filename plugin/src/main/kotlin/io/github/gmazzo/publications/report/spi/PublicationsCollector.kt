package io.github.gmazzo.publications.report.spi

import io.github.gmazzo.publications.report.ReportPublication
import org.gradle.api.Task

public interface PublicationsCollector<Type : Task> {

    public val accepts: Class<out Type>

    public fun collectPublications(task: Type): List<ReportPublication>

}
