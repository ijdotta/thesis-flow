package ar.edu.uns.cs.thesisflow.people.api

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.service.PersonFilter
import ar.edu.uns.cs.thesisflow.people.service.PersonService
import ar.edu.uns.cs.thesisflow.people.service.PersonSpecifications
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/people")
class PersonController(
    val personService: PersonService
) {
    private val allowedParams = setOf("page", "size", "sort", "lastname", "name")
    private val sortableFields = mapOf(
        "lastname" to "lastname",
        "name" to "name"
    )

    @GetMapping
    fun findAll(
        @RequestParam allParams: Map<String, String>,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
        @RequestParam(required = false) lastname: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) sort: String?,
    ): ResponseEntity<*> {
        validateParams(allParams.keys)
        val filter = PersonFilter(
            lastname = lastname?.takeIf { it.isNotBlank() },
            name = name?.takeIf { it.isNotBlank() },
        )
        val pageable = PageRequest.of(page, size, sort.toSort())
        return ResponseEntity.ok(
            personService.findAll(pageable, filter, PersonSpecifications.withFilter(filter))
        )
    }

    @PostMapping
    fun createPerson(@RequestBody person: PersonDTO) =
        ResponseEntity.ok().body(personService.create(person))

    @PutMapping("/{publicId}")
    fun updatePerson(
        @PathVariable("publicId") publicId: String,
        @RequestBody person: PersonDTO
    ): ResponseEntity<*> {
        val personWithId = person.copy(publicId = publicId)
        return ResponseEntity.ok().body(personService.update(personWithId))
    }

    @DeleteMapping("/{id}")
    fun deletePerson(@PathVariable("id") id: String) = personService.delete(id)

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
