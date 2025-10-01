package ar.edu.uns.cs.thesisflow.people.api

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.service.PersonService
import ar.edu.uns.cs.thesisflow.people.validation.CreatePerson
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

import ar.edu.uns.cs.thesisflow.common.ApiPaths
import ar.edu.uns.cs.thesisflow.common.PaginationDefaults

@RestController
@RequestMapping(ApiPaths.PEOPLE)
class PersonController(
    private val personService: PersonService
) {
    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = PaginationDefaults.PAGE_STRING) page: Int,
        @RequestParam(required = false, defaultValue = PaginationDefaults.SIZE_STRING) size: Int,
    ) = ResponseEntity.ok(personService.findAll(PageRequest.of(page, size)))

    @GetMapping("/{publicId}")
    fun findById(@PathVariable publicId: String) = ResponseEntity.ok(personService.findByPublicId(publicId))

    @PostMapping
    fun createPerson(@Validated(CreatePerson::class) @RequestBody person: PersonDTO) =
        ResponseEntity.ok(personService.create(person))

    @PutMapping("/{publicId}")
    fun updatePerson(
        @PathVariable("publicId") publicId: String,
        @RequestBody person: PersonDTO
    ): ResponseEntity<PersonDTO> {
        val personWithId = person.copy(publicId = publicId)
        return ResponseEntity.ok(personService.update(personWithId))
    }
}