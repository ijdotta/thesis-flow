package ar.edu.uns.cs.thesisflow.people.api

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.service.PersonService
import org.slf4j.LoggerFactory
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

@RestController
@RequestMapping("/people")
class PersonController(
    val personService: PersonService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
    ) = try {
        ResponseEntity.ok().body(personService.findAll(PageRequest.of(page, size)))
    } catch (ex: Exception) {
        ResponseEntity.internalServerError().build()
    }

    @PostMapping
    fun createPerson(@RequestBody person: PersonDTO) = try {
        ResponseEntity.ok().body(personService.create(person))
    } catch (ex: Exception) {
        ResponseEntity.internalServerError().build()
    }

    @PutMapping("/{publicId}")
    fun updatePerson(
        @PathVariable("publicId") publicId: String,
        @RequestBody person: PersonDTO
    ) = try {
        val personWithId = person.copy(publicId = publicId)
        ResponseEntity.ok().body(personService.update(personWithId))
    } catch (ex: Exception) {
        ResponseEntity.internalServerError().build()
    }
}