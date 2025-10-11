package ar.edu.uns.cs.thesisflow.catalog.api

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.catalog.service.CareerService
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
import org.springframework.web.bind.annotation.DeleteMapping

@RestController
@RequestMapping("/careers")
class CareerController(
    private val careerService: CareerService
) {
    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
    ) = ResponseEntity.ok(careerService.findAll(PageRequest.of(page, size)))

    @GetMapping("/{publicId}")
    fun findById(@PathVariable publicId: String) = careerService.findByPublicId(publicId)

    @PostMapping
    fun create(@RequestBody career: CareerDTO) = ResponseEntity.ok(careerService.create(career))

    @PutMapping("/{publicId}")
    fun update(@PathVariable publicId: String, @RequestBody careerDTO: CareerDTO) = with(careerDTO) {
        val careerWithId = careerDTO.copy(publicId = publicId)
        ResponseEntity.ok(careerService.update(careerWithId))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: String) = careerService.delete(id)
}