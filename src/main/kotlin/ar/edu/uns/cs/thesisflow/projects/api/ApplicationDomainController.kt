package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.ApplicationDomainDTO
import ar.edu.uns.cs.thesisflow.projects.service.ApplicationDomainService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/application-domains")
class ApplicationDomainController(
    private val service: ApplicationDomainService,
) {

    @GetMapping
    fun findAll() = ResponseEntity.ok(service.findAll())

    @GetMapping("/{publicId}")
    fun findByPublicId(@PathVariable publicId: String) =
        ResponseEntity.ok(service.findByPublicId(publicId))

    @PostMapping
    fun create(@RequestBody entity: ApplicationDomainDTO) = ResponseEntity.ok(service.create(entity))

    @PutMapping("/{publicId}")
    fun update(@PathVariable publicId: String, @RequestBody entity: ApplicationDomainDTO) =
        ResponseEntity.ok(service.update(publicId, entity))
}