package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.service.ProjectService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
        @RequestParam(required = false) completed: Boolean?, // completed=true -> completion NOT NULL; false -> completion NULL
    ): ResponseEntity<*> {
        val filter = ProjectFilter(
            title = title?.takeIf { it.isNotBlank() },
            professorName = professorName?.takeIf { it.isNotBlank() },
            studentName = studentName?.takeIf { it.isNotBlank() },
            domain = domain?.takeIf { it.isNotBlank() },
            completion = completed.toNullabilityFilter(),
        )
        return ResponseEntity.ok(projectService.findAll(PageRequest.of(page, size), filter))
    }

    private fun Boolean?.toNullabilityFilter(): NullabilityFilter? = when (this) {
        null -> null
        true -> NullabilityFilter.NOT_NULL
        false -> NullabilityFilter.NULL
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
