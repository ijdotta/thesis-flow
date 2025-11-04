package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.ApplicationDomainDTO
import ar.edu.uns.cs.thesisflow.projects.service.ApplicationDomainService
import ar.edu.uns.cs.thesisflow.projects.service.ApplicationDomainFilter
import ar.edu.uns.cs.thesisflow.projects.service.ApplicationDomainSpecifications
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/application-domains")
class ApplicationDomainController(
    private val service: ApplicationDomainService,
) {
    private val sortableFields = mapOf(
        "name" to "name",
        "description" to "description"
    )

    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) sort: String?,
    ) = ResponseEntity.ok(service.findAll(
        PageRequest.of(page, size, sort.toSort()),
        ApplicationDomainFilter(
            name = name?.takeIf { it.isNotBlank() },
            description = description?.takeIf { it.isNotBlank() }
        ),
        ApplicationDomainSpecifications.withFilter(
            ApplicationDomainFilter(
                name = name?.takeIf { it.isNotBlank() },
                description = description?.takeIf { it.isNotBlank() }
            )
        )
    ))

    @GetMapping("/{publicId}")
    fun findByPublicId(@PathVariable publicId: String) =
        ResponseEntity.ok(service.findByPublicId(publicId))

    @PostMapping
    fun create(@RequestBody entity: ApplicationDomainDTO) = ResponseEntity.ok(service.create(entity))

    @PutMapping("/{publicId}")
    fun update(@PathVariable publicId: String, @RequestBody entity: ApplicationDomainDTO) =
        ResponseEntity.ok(service.update(publicId, entity))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: String) = service.delete(id)

    private fun String?.toSort(): Sort {
        if (this.isNullOrBlank()) return Sort.unsorted()
        val parts = this.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return Sort.unsorted()
        val fieldKey = parts[0]
        val mappedField = sortableFields[fieldKey]
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort field '$fieldKey'")
        val direction = if (parts.size > 1) parts[1].lowercase() else "asc"
        return if (direction == "desc") Sort.by(mappedField).descending() else Sort.by(mappedField).ascending()
    }
}