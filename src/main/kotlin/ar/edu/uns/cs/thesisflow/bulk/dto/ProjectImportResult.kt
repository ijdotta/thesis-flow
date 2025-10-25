package ar.edu.uns.cs.thesisflow.bulk.dto

import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO

data class ProjectImportResult(
    val lineNumber: Long,
    val title: String?,
    val status: ProjectImportStatus,
    val project: ProjectDTO?,
    val message: String? = null,
)

enum class ProjectImportStatus {
    SUCCESS,
    SKIPPED,
    FAILED,
}
