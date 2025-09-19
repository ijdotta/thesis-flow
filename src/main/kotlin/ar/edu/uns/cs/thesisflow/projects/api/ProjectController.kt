package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.service.ProjectService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val projectService: ProjectService
) {
    @GetMapping
    fun findAll() = ResponseEntity.ok(projectService.findAll())

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String) = ResponseEntity.ok(projectService.findByPublicId(id))

    @PostMapping
    fun create(@RequestBody projectDTO: ProjectDTO) = projectService.create(projectDTO)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody projectDTO: ProjectDTO) =
        projectService.update(id, projectDTO)

    @PutMapping("/{id}/tags")
    fun setTags(@PathVariable id: String, @RequestBody tags: List<String>) =
        projectService.setTags(id, tags)

    @PutMapping("/{id}/application-domain")
    fun setApplicationDomain(
        @PathVariable id: String,
        @RequestBody setApplicationDomainRequest: SetApplicationDomainRequest
    ) = projectService.setApplicationDomain(id, setApplicationDomainRequest.applicationDomain)
}

