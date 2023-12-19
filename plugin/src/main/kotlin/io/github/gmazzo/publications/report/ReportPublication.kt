package io.github.gmazzo.publications.report

import java.io.Serializable

data class ReportPublication(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val repository: Repository,
    val outcome: Outcome,
    val artifacts: List<String>,
) : Serializable {

    data class Repository(val name: String, val value: String): Serializable

    enum class Outcome(text: String? = null) {

        Published, Failed, Skipped, NotRun("not-run"), Unknown;

       val text: String = text ?: name.lowercase()

    }

}
