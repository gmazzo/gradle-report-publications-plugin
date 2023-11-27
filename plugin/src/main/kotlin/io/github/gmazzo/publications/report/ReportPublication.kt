package io.github.gmazzo.publications.report

data class ReportPublication(
    val group: String,
    val artifact: String,
    val version: String,
    val repository: Repository,
    val outcome: Outcome,
) {

    data class Repository(val name: String, val value: String)

    enum class Outcome(text: String? = null) {

        Published, Failed, Skipped, NotRun("not-run"), Unknown;

       val text: String = text ?: name.lowercase()

    }

}
