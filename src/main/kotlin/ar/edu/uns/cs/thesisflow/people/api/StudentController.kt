package ar.edu.uns.cs.thesisflow.people.api

import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.service.StudentService
import ar.edu.uns.cs.thesisflow.people.service.StudentFilter
import ar.edu.uns.cs.thesisflow.people.service.StudentSpecifications
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/students")
class StudentController(
    private val studentService: StudentService,
) {
    private val allowedParams = setOf("page", "size", "sort", "lastname", "name", "studentId", "email")
    private val sortableFields = mapOf(
        "lastname" to "person.lastname",
        "name" to "person.name",
        "studentId" to "studentId",
        "email" to "email"
    )

    @GetMapping
    fun findAll(
        @RequestParam allParams: Map<String, String>,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
        @RequestParam(required = false) lastname: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) studentId: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) sort: String?,
    ): ResponseEntity<*> {
        validateParams(allParams.keys)
        val filter = StudentFilter(
            lastname = lastname?.takeIf { it.isNotBlank() },
            name = name?.takeIf { it.isNotBlank() },
            studentId = studentId?.takeIf { it.isNotBlank() },
            email = email?.takeIf { it.isNotBlank() },
        )
        val pageable = PageRequest.of(page, size, sort.toSort())
        return ResponseEntity.ok(studentService.findAll(pageable, filter, StudentSpecifications.withFilter(filter)))
    }

    @GetMapping("/{publicId}")
    fun findByPublicId(@PathVariable publicId: String) = ResponseEntity
        .ok(studentService.findByPublicId(publicId))

    @PostMapping
    fun create(@RequestBody request: StudentDTO) = ResponseEntity
        .ok(studentService.create(request))

    @PutMapping("/{publicId}")
    fun update(@PathVariable publicId: String, @RequestBody request: StudentDTO) = with (request) {
        val studentWithId = copy(publicId)
        ResponseEntity.ok(studentService.update(studentWithId))
    }

    @PutMapping("/{publicId}/careers")
    fun updateCareers(@PathVariable publicId: String, @RequestBody updateCareersRequest: UpdateCareersRequest) = ResponseEntity
        .ok(studentService.updateCareers(publicId, updateCareersRequest.careers))

    private fun validateParams(paramNames: Set<String>) {
        val invalid = paramNames - allowedParams
        if (invalid.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported filter parameter(s): ${invalid.joinToString(", ")}"
            )
        }
    }

    private fun String?.toSort(): Sort {
        if (this.isNullOrBlank()) return Sort.unsorted()
        val parts = this.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return Sort.unsorted()
        val fieldKey = parts[0]
        val mappedField = sortableFields[fieldKey]
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort field '$fieldKey'")
        val direction = if (parts.size > 1) parts[1].lowercase() else "asc"
        val sortDirection = when (direction) {
            "asc" -> Sort.Direction.ASC
            "desc" -> Sort.Direction.DESC
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort direction '$direction'")
        }
        return Sort.by(Sort.Order(sortDirection, mappedField))
    }
}
