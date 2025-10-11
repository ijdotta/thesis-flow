package ar.edu.uns.cs.thesisflow.projects.bulk

import org.springframework.web.multipart.MultipartFile

interface ProjectCsvParser {
    fun readProjectsFromCsv(file: MultipartFile): List<BulkProjectData>
}

data class BulkProjectData(
    val type: String,
    val submissionDate: String,
    val completionDate: String,
    val completed: Boolean,
    val title: String,
    val director: String,
    val codirector: String?,
    val collaborator: String?,
    val studentA: String,
    val studentB: String,
    val studentC: String,
    val topics: String,
    val domain: String,
)
