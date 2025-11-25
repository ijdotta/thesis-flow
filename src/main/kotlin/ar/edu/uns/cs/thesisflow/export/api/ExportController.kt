package ar.edu.uns.cs.thesisflow.export.api

import ar.edu.uns.cs.thesisflow.export.dto.ExportFilters
import ar.edu.uns.cs.thesisflow.export.service.ExportService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/export")
class ExportController(
    private val exportService: ExportService,
) {

    @GetMapping("/projects")
    @PreAuthorize("hasRole('ADMIN')")
    fun exportProjects(
        @RequestParam(required = false) careerIds: String?,
        @RequestParam(required = false) applicationDomainIds: String?,
        @RequestParam(required = false) tagIds: String?,
        @RequestParam(required = false) professorIds: String?,
        @RequestParam(required = false) projectTypes: String?,
        @RequestParam(required = false) fromYear: Int?,
        @RequestParam(required = false) toYear: Int?,
    ): ResponseEntity<String> {
        val filters = ExportFilters(
            careerIds = careerIds?.split(",")?.map { it.trim() },
            applicationDomainIds = applicationDomainIds?.split(",")?.map { it.trim() },
            tagIds = tagIds?.split(",")?.map { it.trim() },
            professorIds = professorIds?.split(",")?.map { it.trim() },
            projectTypes = projectTypes?.split(",")?.map { it.trim() },
            fromYear = fromYear,
            toYear = toYear,
        )

        val csv = exportService.exportProjectsToCSV(filters)
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val filename = "thesis-flow-export_$timestamp.csv"

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
            .body(csv)
    }
}
