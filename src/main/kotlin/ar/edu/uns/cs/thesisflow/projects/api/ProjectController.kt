package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.service.ProjectService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ar.edu.uns.cs.thesisflow.projects.service.ProjectFilter
import ar.edu.uns.cs.thesisflow.projects.service.NullabilityFilter

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val projectService: ProjectService
) {
    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
        @RequestParam(required = false) title: String?,
        @RequestParam(name = "professor.name", required = false) professorName: String?,
        @RequestParam(name = "student.name", required = false) studentName: String?,
        @RequestParam(required = false) domain: String?,
        @RequestParam(required = false) completion: String?, // expects 'null' or 'not-null'
        @RequestParam(required = false, name = "initialSubmission") initialSubmission: String?, // expects 'null' or 'not-null'
    ): ResponseEntity<*> {
        val filter = ProjectFilter(
            title = title?.takeIf { it.isNotBlank() },
            professorName = professorName?.takeIf { it.isNotBlank() },
            studentName = studentName?.takeIf { it.isNotBlank() },
            domain = domain?.takeIf { it.isNotBlank() },
            completion = completion.toNullability(),
            initialSubmission = initialSubmission.toNullability(),
        )
        return ResponseEntity.ok(projectService.findAll(PageRequest.of(page, size), filter))
    }

    private fun String?.toNullability(): NullabilityFilter? = when (this?.lowercase()) {
        null, "" -> null
        "null" -> NullabilityFilter.NULL
        "not-null", "notnull", "not_null" -> NullabilityFilter.NOT_NULL
        else -> null // silently ignore invalid token; could throw if stricter validation desired
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String) = ResponseEntity.ok(projectService.findByPublicId(id))

    @PostMapping
    fun create(@RequestBody projectDTO: ProjectDTO) = projectService.create(projectDTO)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody projectDTO: ProjectDTO) =
        projectService.update(id, projectDTO)

    @PutMapping("/{id}/tags")
    fun setTags(@PathVariable id: String, @RequestBody setTagsRequest: SetTagsRequest) =
        projectService.setTags(id, setTagsRequest.tagIds)

    @PutMapping("/{id}/application-domain")
    fun setApplicationDomain(
        @PathVariable id: String,
        @RequestBody setApplicationDomainRequest: SetApplicationDomainRequest
    ) = projectService.setApplicationDomain(id, setApplicationDomainRequest.applicationDomain)
}
