package ar.edu.uns.cs.thesisflow.bulk.api

import ar.edu.uns.cs.thesisflow.bulk.LegacyDatasetImporter
import ar.edu.uns.cs.thesisflow.bulk.dto.ProjectImportResult
import ar.edu.uns.cs.thesisflow.bulk.dto.ProjectImportStatus
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/bulk/dataset")
class DatasetImportController(
    private val legacyDatasetImporter: LegacyDatasetImporter,
) {

    @PostMapping(
        "/projects",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun importProjects(@RequestPart("file") file: MultipartFile): ResponseEntity<ProjectImportResponse> {
        if (file.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty")
        }

        val results = file.inputStream.use { stream ->
            legacyDatasetImporter.import(stream, file.originalFilename ?: "upload")
        }
        val summary = ProjectImportSummary.from(results)
        return ResponseEntity.ok(ProjectImportResponse(summary = summary, results = results))
    }
}

data class ProjectImportResponse(
    val summary: ProjectImportSummary,
    val results: List<ProjectImportResult>,
)

data class ProjectImportSummary(
    val total: Int,
    val success: Int,
    val skipped: Int,
    val failed: Int,
) {
    companion object {
        fun from(results: List<ProjectImportResult>): ProjectImportSummary = ProjectImportSummary(
            total = results.size,
            success = results.count { it.status == ProjectImportStatus.SUCCESS },
            skipped = results.count { it.status == ProjectImportStatus.SKIPPED },
            failed = results.count { it.status == ProjectImportStatus.FAILED },
        )
    }
}
