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

@RestController
@RequestMapping("/people")
class PersonController(
    val personService: PersonService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    fun findAll() = try {
        ResponseEntity.ok().body(personService.findAll())
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