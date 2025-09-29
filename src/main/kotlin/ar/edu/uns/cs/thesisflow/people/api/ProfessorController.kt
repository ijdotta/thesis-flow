package ar.edu.uns.cs.thesisflow.people.api

import ar.edu.uns.cs.thesisflow.people.dto.ProfessorDTO
import ar.edu.uns.cs.thesisflow.people.service.ProfessorService
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
@RequestMapping("/professors")
class ProfessorController(
    private val professorService: ProfessorService,
) {
    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
    ) = ResponseEntity.ok(professorService.findAll(PageRequest.of(page, size)))

    @GetMapping("/{publicId}")
    fun findById(@PathVariable publicId: String) = ResponseEntity
        .ok(professorService.findByPublicId(publicId))

    @PostMapping
    fun create(@RequestBody professor: ProfessorDTO) = ResponseEntity
        .ok(professorService.create(professor))

    @PutMapping("/{publicId}")
    fun update(@PathVariable publicId: String, @RequestBody professorDTO: ProfessorDTO) = with(professorDTO) {
        val professorWithId = professorDTO.copy(publicId = publicId)
        ResponseEntity.ok(professorService.update(professorWithId))
    }
}