package io.github.gmazzo.publications.report

import java.io.Serializable

public data class ReportPublication(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val repository: Repository,
    val outcome: Outcome,
    val artifacts: List<String>,
) : Serializable {

    public data class Repository(val name: String, val value: String) : Serializable

    public enum class Outcome { Published, Failed, Skipped, Unknown }

}
