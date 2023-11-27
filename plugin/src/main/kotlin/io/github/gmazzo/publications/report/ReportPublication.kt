package io.github.gmazzo.publications.report

data class ReportPublication(
    val group: String,
    val artifact: String,
    val version: String,
    val repository: Repository,
    val outcome: Outcome,
) {

    data class Repository(val name: String, val value: String)

    enum class Outcome { Published, Failed, Skipped, Unknown }

}
